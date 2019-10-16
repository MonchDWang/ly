package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.entity.TbCategoryBrand;
import com.leyou.item.mapper.TbBrandMapper;
import com.leyou.item.pojo.DTO.BrandDTO;
import com.leyou.item.service.TbBrandService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.item.service.TbCategoryBrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
@Service
public class TbBrandServiceImpl extends ServiceImpl<TbBrandMapper, TbBrand> implements TbBrandService {

    @Autowired
    private TbCategoryBrandService categoryBrandService;
    /**
     * 品牌的分页
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @Override
    public PageResult<BrandDTO> findBypage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //构造分页的 参数
        Page<TbBrand> page1 = new Page<>(page,rows);
        //构造查询条件
        QueryWrapper<TbBrand> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            //select * from tb_brand where name like '%?%'  or letter like '%?%'
            queryWrapper.lambda().like(TbBrand::getName,key).or().like(TbBrand::getLetter,key);
        }
        if(!StringUtils.isEmpty(sortBy)){
            if(desc){
                queryWrapper.orderByDesc(sortBy);
            }else{
                queryWrapper.orderByAsc(sortBy);
            }
        }
        //分页的结果
        IPage<TbBrand> tbBrandIPage = this.page(page1, queryWrapper);
        if(tbBrandIPage == null || CollectionUtils.isEmpty(tbBrandIPage.getRecords())){
            throw  new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        List<BrandDTO> brandDTOList = BeanHelper.copyWithCollection(tbBrandIPage.getRecords(), BrandDTO.class);
        //构造分页结果
        return new PageResult<BrandDTO>(tbBrandIPage.getTotal(),
                Integer.valueOf(String.valueOf(tbBrandIPage.getPages())),
                brandDTOList);
    }

    /**
     * 保存品牌
     * 保存品牌 分类中间表
     * @param brand
     */
    @Override
    public void saveBrand(TbBrand brand,List<Long> cids) {
        boolean b = this.save(brand);
        if(!b){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //品牌id
        Long brandId = brand.getId();
        //保存中间表信息
//        List<TbCategoryBrand> categoryBrands = new ArrayList<>();
//        for(Long cid:cids){
//            TbCategoryBrand tbCategoryBrand = new TbCategoryBrand();
//            tbCategoryBrand.setBrandId(brandId);
//            tbCategoryBrand.setCategoryId(cid);
//            categoryBrands.add(tbCategoryBrand);
//        }
        List<TbCategoryBrand> categoryBrands = cids.stream().map(cid -> {
            TbCategoryBrand tbCategoryBrand = new TbCategoryBrand();
            tbCategoryBrand.setCategoryId(cid);
            tbCategoryBrand.setBrandId(brandId);
            return tbCategoryBrand;
        }).collect(Collectors.toList());

        categoryBrandService.saveBatch(categoryBrands);
    }

    @Override
    public List<BrandDTO> findBrandListByCategoryId(Long cid) {
        List<TbBrand>  tbBrandList = this.getBaseMapper().selectBrandListByCategoryId(cid);
        if(CollectionUtils.isEmpty(tbBrandList)){
            throw  new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbBrandList,BrandDTO.class);
    }
}
