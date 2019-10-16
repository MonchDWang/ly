package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.DTO.SkuDTO;
import com.leyou.item.pojo.DTO.SpecGroupDTO;
import com.leyou.item.pojo.DTO.SpuDTO;
import com.leyou.item.pojo.DTO.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询spu信息
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> findSpuByPage(@RequestParam(name = "page",defaultValue = "1") Integer page,
                                                            @RequestParam(name = "rows",defaultValue = "5") Integer rows,
                                                            @RequestParam(name = "key",required = false) String key,
                                                            @RequestParam(name = "saleable",required = false) Boolean saleable){
        return ResponseEntity.ok(goodsService.findSpuPage(page,rows,key,saleable));
    }

    /**
     * 新增商品
     * @param spuDTO
     * @return
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO){
            goodsService.saveGoods(spuDTO);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 上下架
     * @param saleable
     * @param spuId
     * @return
     */
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSaleable(@RequestParam(name = "saleable") Boolean saleable,
                                               @RequestParam(name = "id") Long spuId){
        goodsService.updateSaleable(saleable,spuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 查询spuDetail 的信息
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetailDTO> findSpuDetailBySpuId(@RequestParam(name = "id")Long spuId){
        return ResponseEntity.ok(goodsService.findSpuDetailBySpuId(spuId));
    }

    /**
     * 根据spuid查询sku集合
     * @param spuId
     * @return
     */
    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<SkuDTO>> findSkuListBySpuId(@RequestParam(name = "id")Long spuId){
        return ResponseEntity.ok(goodsService.findSkuListBySpuId(spuId));
    }

    /**
     * 修改商品
     * @param spuDTO
     * @return
     */
    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spuid  查询spu的信息
     * @param spuId
     * @return
     */
    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuDTO> findSpuBySpuId(@PathVariable(name = "id") Long spuId){

        return ResponseEntity.ok(goodsService.findSpuBySpuId(spuId));
    }

    /**
     * 根据分类id ，查询分组信息，和组内的规格参数名字
     * @param cid3
     * @return
     */
    @GetMapping("/spec/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroup(@RequestParam(name = "id")Long cid3){
        return ResponseEntity.ok(goodsService.findSpecGroup(cid3));
    }

    /**
     * 根据ids 查询sku的集合
     * @param ids
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<SkuDTO>> findSkuListByIds(@RequestParam(name = "ids")List<Long> ids){
        return ResponseEntity.ok(goodsService.findSkuListByIds(ids));
    }

    /**
     * 减库存
     * @param map
     * @return
     */
    @PutMapping("/stock/minus")
    public ResponseEntity<Void> minusSkuStock(@RequestBody Map<Long,Integer> map){
        goodsService.stockMinus(map);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 增加库存
     * @param skuMap
     */
    @PutMapping("/stock/plus")
    public ResponseEntity<Void> plusStock(@RequestBody  Map<Long, Integer> skuMap){
        goodsService.stockPlus(skuMap);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
