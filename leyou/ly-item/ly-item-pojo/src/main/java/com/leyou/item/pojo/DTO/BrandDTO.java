package com.leyou.item.pojo.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BrandDTO {


    /**
     * 品牌id
     */
    private Long id;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 品牌图片地址
     */
    private String image;

    /**
     * 品牌的首字母
     */
    private String letter;


}
