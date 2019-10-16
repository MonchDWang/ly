package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//继承SpringDataElasticSearch提供的工具类
// ElasticsearchRepository 提供了 简单的crud
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
