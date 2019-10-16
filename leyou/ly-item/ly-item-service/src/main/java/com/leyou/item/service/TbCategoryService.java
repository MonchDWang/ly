package com.leyou.item.service;

import com.leyou.item.entity.TbCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.item.pojo.DTO.CategoryDTO;

import java.util.List;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 服务类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
public interface TbCategoryService extends IService<TbCategory> {

    List<CategoryDTO> findCategoryListByParentId(Long pid);

    List<CategoryDTO> findCategoryListByBrandId(Long brandId);

    List<CategoryDTO> findCategoryListByIds(List<Long> cids);
}
