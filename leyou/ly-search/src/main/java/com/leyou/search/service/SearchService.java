package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.DTO.*;
import com.leyou.search.DTO.GoodsDTO;
import com.leyou.search.DTO.SearchRequest;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ItemClient itemClient;
    /**
     * 构造goods对象
     * @return
     */
    public Goods createGoods(SpuDTO spuDTO){
        Long spuId = spuDTO.getId();
        //获取 全部3级分类的 名称
        //所有分类id的集合
        List<Long> categoryIds = spuDTO.getCategoryIds();
        //查询分类的集合，根据分类id的集合
        List<CategoryDTO> categoryDTOList = itemClient.findCategoryListByIds(categoryIds);
        //获取所有的分类名字
        String categoryNames = categoryDTOList.stream().map(CategoryDTO::getName).collect(Collectors.joining(","));
        //获取品牌信息
        BrandDTO brandDTO = itemClient.findBrandById(spuDTO.getBrandId());
        String spuTitle = spuDTO.getName();
        //构造all的内容
        String all = categoryNames + "," +brandDTO.getName() +","+ spuTitle;
        //通过spuid获取sku的集合
        List<SkuDTO> skuDTOList = itemClient.findSkuListBySpuId(spuId);
        //在es中，sku的信息不需要这么多，我们只需要的数据
//        skuid，title,image,price
        List<Map<String,Object>> skuMap = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            Map<String,Object> map = new HashMap();
            map.put("id",skuDTO.getId());
            map.put("title",skuDTO.getTitle());
            //image可能有多个，我们只需要第一个
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(),","));
            map.put("price",skuDTO.getPrice());
            skuMap.add(map);
        }
        //获取所有sku的price，组成一个set集合
        Set<Long> price = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());
        //获取分类对应的 用于搜索的 规格参数，都是参数的名字
        List<SpecParamDTO> paramDTOList = itemClient.findSpecParamList(null, spuDTO.getCid3(), true);
        //获取spu对应的规格参数的值，存在spuDetail中
        SpuDetailDTO spuDetailDTO = itemClient.findSpuDetailBySpuId(spuId);
        //获取通过的规格参数 json数据
        String genericSpecJson = spuDetailDTO.getGenericSpec();
        //构造 通过参数的  map结构  key- param的id   value -规格参数的值
        Map<Long, Object> genericMap = JsonUtils.toMap(genericSpecJson, Long.class, Object.class);
//        获取特殊的规格参数 json数据
        String specialSpecJson = spuDetailDTO.getSpecialSpec();
        //构造特殊的规格参数的map结构  ，key param的id  value 就是值的集合
        Map<Long, List<String>> specialMap = JsonUtils.nativeRead(specialSpecJson, new TypeReference<Map<Long, List<String>>>() {
        });
        //存入es 中的数据，key   规格参数的名字  value  参数对应的值
        Map<String, Object> specs = new HashMap<>();
        for(SpecParamDTO paramDTO: paramDTOList){
            Long paramId = paramDTO.getId();
            //specs 的key 是 规格参数的名字
            String key = paramDTO.getName();
            //获取这个规格参数是否是通用的
            Boolean generic = paramDTO.getGeneric();
            Object value = null;
            if(generic){
                value = genericMap.get(paramId);
            }else{
                value = specialMap.get(paramId);
            }
            //判断是否是数字类型，如果是的话，就需要处理 值的区间
            Boolean isNumeric = paramDTO.getIsNumeric();
            if(isNumeric){
                value = chooseSegment(value,paramDTO);
            }
            specs.put(key,value);
        }
        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setSubTitle(spuDTO.getSubTitle());
        //放 分类的名字 品牌的名字  spu的名字
        goods.setAll(all);
        goods.setSkus(JsonUtils.toString(skuMap));
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setPrice(price);
        goods.setSpecs(specs);
        goods.setCreateTime(spuDTO.getCreateTime().getTime());
        return goods;
    }


    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        //value = 3000
        //0-2000,2000-3000,3000-4000,4000-
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }

    @Autowired
    private ElasticsearchTemplate esTemplate;
    /**
     * 用户输入关键词，进行搜索
     * @param request
     * @return
     */
    public PageResult<GoodsDTO> search(SearchRequest request) {

        //获取 用户输入的 参数
        String key = request.getKey();
        Integer page = request.getPage()-1;
        Integer size = request.getSize();
//        参数的检查
//        1、构造一个原生查询构建起
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//        2、过滤返回的结果字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
//        3、进行关键词检索 ,match 默认是 OR
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key).operator(Operator.AND));
        queryBuilder.withQuery(this.baseQuery(request));
//        4、处理分页
        queryBuilder.withPageable(PageRequest.of(page,size));
//        5、发送查询内容 ,接收返回值
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
//        6、构造PageResult
        long total = aggregatedPage.getTotalElements();
        int totalPages = aggregatedPage.getTotalPages();
        List<Goods> goodsList = aggregatedPage.getContent();
        if(CollectionUtils.isEmpty(goodsList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(goodsList, GoodsDTO.class);
        return new PageResult<GoodsDTO>(total,totalPages,goodsDTOS);
    }

    public Map<String,List<?>> searchFilter(SearchRequest request) {
        Map<String,List<?>> filterMap = new HashMap<>();
        String key = request.getKey();
//        1、构造原生查询器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//      2、过滤 结果字段 ,什么字段都不需要
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
//        3、进行关键词检索 match查询默认是 OR
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key).operator(Operator.AND));
        queryBuilder.withQuery(this.baseQuery(request));
//        4、处理分页，不关心结果，所以只查一条数据
        queryBuilder.withPageable(PageRequest.of(0,1));

        //配置聚合 信息
        //定义 分类的聚合名字
        String categoryAgg = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId"));
        //定义品牌的聚合
        String brandAgg = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));
//        5、把query对象 发送到es中
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        //获取所有的聚合结果
        Aggregations aggregations = aggregatedPage.getAggregations();
        //获取分类的聚合结果,内容都是id，
        LongTerms categoryTerms = aggregations.get(categoryAgg);
        //查询id对应的值
        List<Long> categoryIds = handleCategory(categoryTerms,filterMap);
        //获取品牌的聚合结果，内容都是id，
        LongTerms brandTerms = aggregations.get(brandAgg);
        handleBrand(brandTerms,filterMap);
        //当搜索结果中的分类 =1 的时候，查询规格参数的过滤条件
        if(!CollectionUtils.isEmpty(categoryIds) && categoryIds.size() == 1){
            //进行  规格参数的 聚合,重新查询一次关键词，并且配置聚合的条件
            handleSpec(request,categoryIds.get(0),filterMap);
        }
        return filterMap;
    }

    /**
     * 进行规格参数的聚合
     * @param request
     * @param filterMap
     */
    private void handleSpec(SearchRequest request, Long cid,Map<String, List<?>> filterMap) {
        String key = request.getKey();
//        1、构造原生查询器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//      2、过滤 结果字段 ,什么字段都不需要
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
//        3、进行关键词检索 match查询默认是 OR
        queryBuilder.withQuery(this.baseQuery(request));
//        4、处理分页，不关心结果，所以只查一条数据
        queryBuilder.withPageable(PageRequest.of(0,1));
        //从item服务中获取 规格参数的集合
        List<SpecParamDTO> specParamList = itemClient.findSpecParamList(null, cid, true);
        for (SpecParamDTO paramDTO : specParamList) {
            String name = paramDTO.getName();
            String field = "specs."+name;
            //设置聚合条件
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field(field));
        }
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = aggregatedPage.getAggregations();
        for (SpecParamDTO paramDTO : specParamList) {
            String name = paramDTO.getName();
            //当前规格参数的 聚合结果
            StringTerms specTerm = aggregations.get(name);
            //得到规格参数的 聚合结果
            List<String> specAgg = specTerm.getBuckets().stream().map(StringTerms.Bucket::getKeyAsString).collect(Collectors.toList());
            filterMap.put(name,specAgg);
        }

    }

    /**
     * 封装基本查询
     * @param request
     * @return
     */
    private QueryBuilder baseQuery(SearchRequest request){
        //搜索的关键词
        String key = request.getKey();
        Map<String, String> filterMap = request.getFilter();
        //        构造 布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("all",key).operator(Operator.AND));
        if(!CollectionUtils.isEmpty(filterMap)){
            for (String filterKey : filterMap.keySet()) {
                String fieldName = "specs."+filterKey;
                if(filterKey.equals("分类")){
                    fieldName = "categoryId";
                }
                else if(filterKey.equals("品牌")){
                    fieldName = "brandId";
                }
                //选择的过滤条件
                String value = filterMap.get(filterKey);
                queryBuilder.filter(QueryBuilders.termQuery(fieldName,value));
            }
        }
        return queryBuilder;
    }

    private void handleBrand(LongTerms brandTerms, Map<String, List<?>> filterMap) {
        //获取品牌的id集合
        List<Long> brandIds = brandTerms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        List<BrandDTO> brandDTOList = itemClient.findBrandListByIds(brandIds);
        filterMap.put("品牌",brandDTOList);
    }

    private List<Long>  handleCategory(LongTerms categoryTerms, Map<String, List<?>> filterMap) {
        //获取 桶
        List<LongTerms.Bucket> buckets = categoryTerms.getBuckets();
        List<Long> categoryIds = new ArrayList<>();
        //循环 桶，获取桶的数据，只要key
        for (LongTerms.Bucket bucket : buckets) {
            long categoryId = bucket.getKeyAsNumber().longValue();
            categoryIds.add(categoryId);
        }
        //获取category的集合
        List<CategoryDTO> categoryDTOList = itemClient.findCategoryListByIds(categoryIds);
        filterMap.put("分类",categoryDTOList);
        return categoryIds;
    }
}
