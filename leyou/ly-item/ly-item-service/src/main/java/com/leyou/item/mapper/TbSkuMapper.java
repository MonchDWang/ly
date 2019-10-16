package com.leyou.item.mapper;

import com.leyou.item.entity.TbSku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * sku表,该表表示具体的商品实体,如黑色的 64g的iphone 8 Mapper 接口
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
public interface TbSkuMapper extends BaseMapper<TbSku> {

    @Update("update tb_sku set stock=stock-#{num} where id=#{skuId}")
    int minusStock(@Param("skuId") Long skuId, @Param("num") Integer num);

    @Update("update tb_sku set stock=stock+#{num} where id =#{skuId}")
    int plusStock(@Param("skuId")Long skuId, @Param("num")Integer num);
}
