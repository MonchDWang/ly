package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.DTO.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("item-service")
public interface ItemClient {

    /**
     * 根据cids 获取分类集合
     * @param cids
     * @return
     */
    @GetMapping("/category/list")
    List<CategoryDTO> findCategoryListByIds(@RequestParam(name = "ids") List<Long> cids);

    /**
     * 根据id获取 品牌
     * @param brandId
     * @return
     */
    @GetMapping("/brand/{id}")
    BrandDTO findBrandById(@PathVariable(name = "id") Long brandId);

    /**
     * 根据spuid 查询sku集合
     * @param spuId
     * @return
     */
    @GetMapping("/sku/of/spu")
    List<SkuDTO> findSkuListBySpuId(@RequestParam(name = "id")Long spuId);

    /**
     * 获取规格参数的集合
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("/spec/params")
    List<SpecParamDTO> findSpecParamList(@RequestParam(name = "gid",required = false) Long gid,
                                                                @RequestParam(name = "cid" ,required = false) Long cid,
                                                                @RequestParam(name = "searching",required = false) Boolean searching);

    /**
     * 查询spuDetail 的信息
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail")
    SpuDetailDTO findSpuDetailBySpuId(@RequestParam(name = "id")Long spuId);

    /**
     * 分页查询spu信息
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<SpuDTO> findSpuByPage(@RequestParam(name = "page",defaultValue = "1") Integer page,
                                     @RequestParam(name = "rows",defaultValue = "5") Integer rows,
                                     @RequestParam(name = "key",required = false) String key,
                                     @RequestParam(name = "saleable",required = false) Boolean saleable);

    /**
     * 根据品牌id的集合 查询品牌的集合
     * @param brandIds
     * @return
     */
    @GetMapping("/brand/list")
    List<BrandDTO> findBrandListByIds(@RequestParam(name = "ids") List<Long> brandIds);

    /**
     * 根据spuid  查询spu的信息
     * @param spuId
     * @return
     */
    @GetMapping("/spu/{id}")
    SpuDTO findSpuBySpuId(@PathVariable(name = "id") Long spuId);

    /**
     * 根据分类id ，查询分组信息，和组内的规格参数名字
     * @param cid3
     * @return
     */
    @GetMapping("/spec/of/category")
    List<SpecGroupDTO> findSpecGroup(@RequestParam(name = "id") Long cid3);

    /**
     * 根据skuid的集合 获取sku的集合
     * @param skuIdList
     * @return
     */
    @GetMapping("/sku/list")
    List<SkuDTO> findSkuListByIds(@RequestParam(name = "ids") List<Long> skuIdList);

    /**
     * 减库存
     * @param map
     * @return
     */
    @PutMapping("/stock/minus")
    Void minusSkuStock(@RequestBody Map<Long,Integer> map);

    /**
     * 增加库存
     * @param skuMap
     */
    @PutMapping("/stock/plus")
    void plusStock(@RequestBody  Map<Long, Integer> skuMap);
}
