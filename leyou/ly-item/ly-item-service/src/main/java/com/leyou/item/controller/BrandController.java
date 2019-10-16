package com.leyou.item.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.pojo.DTO.BrandDTO;
import com.leyou.item.service.TbBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private TbBrandService brandService;

    /**
     * 品牌分页查询
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<BrandDTO>> findBypage(@RequestParam(name = "key",required = false) String key,
                                                           @RequestParam(name = "page",defaultValue = "1") Integer page,
                                                           @RequestParam(name = "rows",defaultValue = "10") Integer rows,
                                                           @RequestParam(name = "sortBy",required = false) String sortBy,
                                                           @RequestParam(name = "desc",defaultValue = "false") Boolean desc){
        return ResponseEntity.ok(brandService.findBypage(key,page,rows,sortBy,desc));
    }

    /**
     * 保存品牌信息
     * @param brand
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> save(TbBrand brand,@RequestParam(name = "cids") List<Long> cids){
            brandService.saveBrand(brand,cids);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 根据分类id查询品牌信息集合
     * @param cid
     * @return
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> findBrandListByCategoryId(@RequestParam(name = "id") Long cid){

        return ResponseEntity.ok(brandService.findBrandListByCategoryId(cid));
    }
    /**
     * 根据id获取 品牌
     * @param brandId
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> findBrandById(@PathVariable(name = "id") Long brandId){
        TbBrand tbBrand = brandService.getById(brandId);
        if(tbBrand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return ResponseEntity.ok(BeanHelper.copyProperties(tbBrand,BrandDTO.class));
    }

    /**
     * 根据品牌id的集合 查询品牌的集合
     * @param brandIds
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<BrandDTO>> findBrandListByIds(@RequestParam(name = "ids") List<Long> brandIds){
        Collection<TbBrand> tbBrandCollection = brandService.listByIds(brandIds);
        if(CollectionUtils.isEmpty(tbBrandCollection)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        List<TbBrand> tbBrandList = (List<TbBrand>)tbBrandCollection;
        return ResponseEntity.ok(BeanHelper.copyWithCollection(tbBrandList,BrandDTO.class));
    }
}
