package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.mapper.TbCategoryMapper;
import com.leyou.item.pojo.DTO.CategoryDTO;
import com.leyou.item.service.TbCategoryBrandService;
import com.leyou.item.service.TbCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
@Service
public class TbCategoryServiceImpl extends ServiceImpl<TbCategoryMapper, TbCategory> implements TbCategoryService {

    /**
     * 根据父id查询 分类集合
     * @param pid
     * @return
     */
    @Override
    public List<CategoryDTO> findCategoryListByParentId(Long pid) {
        QueryWrapper<TbCategory> queryWrapper = new QueryWrapper<>();
        //select * from tb_category where parent_id=?
//        queryWrapper.eq("parent_id",pid);
        queryWrapper.lambda().eq(TbCategory::getParentId,pid);
        List<TbCategory> tbCategoryList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbCategoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbCategoryList,CategoryDTO.class);

    }
    @Autowired
    private TbCategoryBrandService categoryBrandService;

    @Override
    public List<CategoryDTO> findCategoryListByBrandId(Long brandId) {

        List<TbCategory> tbCategoryList = this.getBaseMapper().selectCategoryByBrandId(brandId);
        if(CollectionUtils.isEmpty(tbCategoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbCategoryList,CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> findCategoryListByIds(List<Long> cids) {
        Collection<TbCategory> tbCategoryCollection = this.listByIds(cids);
        if(CollectionUtils.isEmpty(tbCategoryCollection)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<TbCategory> tbCategoryList = (List<TbCategory>)tbCategoryCollection;

        return BeanHelper.copyWithCollection(tbCategoryList,CategoryDTO.class);
    }
}
