package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.constants.MQConstants;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.*;
import com.leyou.item.pojo.DTO.*;
import lombok.Synchronized;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsService {


    @Autowired
    private TbSpuService spuService;
    /**
     * 分页查询spu
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    public PageResult<SpuDTO> findSpuPage(Integer page, Integer rows, String key, Boolean saleable) {
        Page<TbSpu> page1 = new Page<>(page,rows);
        QueryWrapper<TbSpu> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            queryWrapper.lambda().like(TbSpu::getName,key);
        }
        if(saleable != null){
            queryWrapper.lambda().eq(TbSpu::getSaleable,saleable);
        }

        IPage<TbSpu> tbSpuIPage = spuService.page(page1, queryWrapper);
        if(tbSpuIPage == null || CollectionUtils.isEmpty(tbSpuIPage.getRecords())){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(tbSpuIPage.getRecords(), SpuDTO.class);

        handlerCategoryNameAndBrandName(spuDTOList);

        return new PageResult(tbSpuIPage.getTotal(),Integer.valueOf(String.valueOf(tbSpuIPage.getPages())),spuDTOList);
    }

    @Autowired
    private TbBrandService brandService;
    @Autowired
    private TbCategoryService categoryService;
    private void handlerCategoryNameAndBrandName(List<SpuDTO> spuDTOList) {
        for (SpuDTO spuDTO : spuDTOList) {
            Long brandId = spuDTO.getBrandId();
            TbBrand tbBrand = brandService.getById(brandId);
            spuDTO.setBrandName(tbBrand.getName());

            List<Long> categoryIds = spuDTO.getCategoryIds();
            Collection<TbCategory> collection = categoryService.listByIds(categoryIds);
//            String categoryName = "";
//            for (TbCategory tbCategory : collection) {
//                String cName = tbCategory.getName();
//                if(!StringUtils.isEmpty(categoryName)){
//                    categoryName += "/";
//                }
//                categoryName += cName;
//            }
            String categoryName = collection.stream().map(category -> {
                return category.getName();
            }).collect(Collectors.joining("/"));

            spuDTO.setCategoryName(categoryName);
        }
    }

    @Autowired
    private TbSpuDetailService detailService;
    @Autowired
    private TbSkuService skuService;
    /**
     * 保存商品信息
     * @param spuDTO
     */
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
//        1、保存spu
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        boolean bSpu = spuService.save(tbSpu);
        if(!bSpu){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //spu的id
        Long spuId = tbSpu.getId();
//        2、保存spudetail
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
        tbSpuDetail.setSpuId(spuId);
        boolean bDetail = detailService.save(tbSpuDetail);

        if(!bDetail){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//        3、保存sku
        List<SkuDTO> skuDTOList = spuDTO.getSkus();

        List<TbSku> skuList = skuDTOList.stream().map(skuDTO -> {
            skuDTO.setSpuId(spuId);
            return BeanHelper.copyProperties(skuDTO, TbSku.class);
        }).collect(Collectors.toList());

        boolean bSku = skuService.saveBatch(skuList);
        if(!bSku){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 修改上下架
     * @param saleable
     * @param spuId
     */
    @Transactional
    public void updateSaleable(Boolean saleable, Long spuId) {
//        1、修改spu的上下架
        TbSpu tbSpu = new TbSpu();
        tbSpu.setId(spuId);
        tbSpu.setSaleable(saleable);
        boolean bSpu = spuService.updateById(tbSpu);
        if(!bSpu){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
//        2、修改sku
        // update tb_sku  set  enable =? where  spu_id = ?
        UpdateWrapper<TbSku> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(TbSku::getSpuId,spuId);
        updateWrapper.lambda().set(TbSku::getEnable,saleable);
        boolean bSku = skuService.update(updateWrapper);
        if(!bSku){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //发送  上下架的消息 ，给rabbitmq ,说明交换机，内容，routingkey
        String routingKey = saleable?MQConstants.RoutingKey.ITEM_UP_KEY:MQConstants.RoutingKey.ITEM_DOWN_KEY;
        amqpTemplate.convertAndSend(MQConstants.Exchange.ITEM_EXCHANGE_NAME,routingKey,spuId);
    }

    /**
     * 查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetailDTO findSpuDetailBySpuId(Long spuId) {
        TbSpuDetail tbSpuDetail = detailService.getById(spuId);
        if(tbSpuDetail == null){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(tbSpuDetail,SpuDetailDTO.class);
    }

    /**
     * 查询sku'集合
     * @param spuId
     * @return
     */
    public List<SkuDTO> findSkuListBySpuId(Long spuId) {
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,spuId);
        List<TbSku> tbSkuList = skuService.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbSkuList)){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSkuList,SkuDTO.class);
    }

    /**
     * 修改商品
     *
     * @param spuDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(SpuDTO spuDTO) {
        Long spuId = spuDTO.getId();
//         1、修改spu，包含id
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        boolean bSpu = spuService.updateById(tbSpu);
        if(!bSpu){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
//        2、修改detail，包含spuId
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
        boolean bSpuDetail = detailService.updateById(tbSpuDetail);
        if(!bSpuDetail){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
//        3、修改sku
//        3.1、删除sku的信息，条件时spuid
        // delete from tb_sku where spu_id = ?
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,spuId);
        boolean bRemoveSku = skuService.remove(queryWrapper);
        if(!bRemoveSku){
            throw  new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
//        3.2、新增sku,需要把spuId设置进去
        List<SkuDTO> skuDTOList = spuDTO.getSkus();

//        List<TbSku> tbSkuList = new ArrayList<>();
//        for (SkuDTO skuDTO : skuDTOList) {
//            skuDTO.setSpuId(spuId);
//            TbSku tbSku = BeanHelper.copyProperties(skuDTO, TbSku.class);
//            tbSkuList.add(tbSku);
//        }
        List<TbSku> tbSkuList = skuDTOList.stream().map(skuDTO -> {
            skuDTO.setSpuId(spuId);
            return BeanHelper.copyProperties(skuDTO, TbSku.class);
        }).collect(Collectors.toList());

        boolean bSaveSku = skuService.saveBatch(tbSkuList);
        if(!bSaveSku){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 查询spu的信息
     * @param spuId
     * @return
     */
    public SpuDTO findSpuBySpuId(Long spuId) {
        TbSpu tbSpu = spuService.getById(spuId);
        if(tbSpu == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(tbSpu,SpuDTO.class);
    }

    @Autowired
    private TbSpecGroupService groupService;
    @Autowired
    private TbSpecParamService paramService;
    public List<SpecGroupDTO> findSpecGroup(Long cid3) {
        //分组的信息
//        分组对应的 参数的信息
        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecGroup::getCid,cid3);
        List<TbSpecGroup> tbSpecGroupList = groupService.list(queryWrapper);
        List<SpecGroupDTO> specGroupDTOList = BeanHelper.copyWithCollection(tbSpecGroupList, SpecGroupDTO.class);
        /*
        for (SpecGroupDTO specGroupDTO : specGroupDTOList) {
            Long groupId = specGroupDTO.getId();
            //通过groupid 查询 对应的 规格参数集合
            QueryWrapper<TbSpecParam> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda().eq(TbSpecParam::getGroupId,groupId);
            //循环查询数据库
            List<TbSpecParam> tbSpecParamList = paramService.list(queryWrapper1);
            List<SpecParamDTO> specParamDTOS = BeanHelper.copyWithCollection(tbSpecParamList, SpecParamDTO.class);
            specGroupDTO.setParams(specParamDTOS);
        }*/
        /*
        QueryWrapper<TbSpecParam> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().eq(TbSpecParam::getCid,cid3);
        List<TbSpecParam> tbSpecParamList = paramService.list(queryWrapper1);
        for (SpecGroupDTO specGroupDTO : specGroupDTOList) {
            Long groupId = specGroupDTO.getId();
            List<SpecParamDTO> paramDTOList = new ArrayList<>();
            for(TbSpecParam specParam : tbSpecParamList){
                if(specParam.getGroupId().longValue() == groupId.longValue()){
                    paramDTOList.add(BeanHelper.copyProperties(specParam,SpecParamDTO.class));
                }
            }
            specGroupDTO.setParams(paramDTOList);
        }*/
        QueryWrapper<TbSpecParam> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().eq(TbSpecParam::getCid,cid3);
        List<TbSpecParam> tbSpecParamList = paramService.list(queryWrapper1);
        List<SpecParamDTO> paramDTOList = BeanHelper.copyWithCollection(tbSpecParamList, SpecParamDTO.class);
        //Map<groupId,List<specParam>>
        Map<Long, List<SpecParamDTO>> groupParamMap = paramDTOList.stream().collect(Collectors.groupingBy(SpecParamDTO::getGroupId));
        for (SpecGroupDTO specGroupDTO : specGroupDTOList) {
            specGroupDTO.setParams(groupParamMap.get(specGroupDTO.getId()));
        }
        return specGroupDTOList;
    }

    /**
     * 根据ids 查询sku的集合
     * @param ids
     * @return
     */
    public List<SkuDTO> findSkuListByIds(List<Long> ids) {
        Collection<TbSku> tbSkuCollection = skuService.listByIds(ids);
        List<TbSku> tbSkuList = (List<TbSku>)tbSkuCollection;
        if(CollectionUtils.isEmpty(tbSkuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSkuList,SkuDTO.class);
    }

    /**
     * 减库存
     * 去数据库 减数
     * select stock from sku where id =#{skuId}
     * if(stock>=num){
     *  update sku set stock=stock-#{num}  where id=#{skuId} and stock>num
     * }
     * @param map
     */
    public void stockMinus(Map<Long, Integer> map) {
        skuService.stockMinus(map);
    }

    /**
     * 加库存
     * @param skuMap
     */
    public void stockPlus(Map<Long, Integer> skuMap) {
        skuService.stockPlus(skuMap);
    }
}
