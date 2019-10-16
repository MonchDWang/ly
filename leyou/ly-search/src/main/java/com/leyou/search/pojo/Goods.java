package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;
import java.util.Set;

/**
 * 一个goods对象  对应 一条spu信息
 */
@Data
@Document(indexName = "goods",type = "docs",shards = 1,replicas = 1)
public class Goods {
    //spuId
    @Id
    @Field(type = FieldType.Keyword)
    private Long id;
    @Field(type = FieldType.Keyword,index = false)
    private String subTitle;
    //Sku集合的json字符串，只用来显示，索引不分词，不用建立索引
    @Field(type = FieldType.Keyword,index = false)
    private String skus;
    //存储 用来搜索的内容，可以包含  分类名 品牌的名字  title
    @Field(type=FieldType.Text,analyzer = "ik_max_word")
    private String all;
    private Long brandId;// 品牌id
    private Long categoryId;// 商品第3级分类id
    private Long createTime;// spu创建时间
    private Set<Long> price;// 价格
    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，value是参数值
}
