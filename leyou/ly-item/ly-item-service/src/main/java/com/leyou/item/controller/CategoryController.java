package com.leyou.item.controller;

import com.leyou.item.pojo.DTO.CategoryDTO;
import com.leyou.item.service.TbCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private TbCategoryService categoryService;

    /**
     * 根据父id 查询分类集合
     * @param pid
     * @return
     */
    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByParentId(@RequestParam(name = "pid") Long pid){
        return ResponseEntity.ok(categoryService.findCategoryListByParentId(pid));
    }

    /**
     * 查询品牌所属的分类信息
     * @param brandId
     * @return
     */
    @GetMapping("/of/brand")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByBrandId(@RequestParam(name = "id") Long brandId){
        return ResponseEntity.ok(categoryService.findCategoryListByBrandId(brandId));
    }

    /**
     * 根据cids 获取分类集合
     * @param cids
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByIds(@RequestParam(name = "ids") List<Long> cids){
        return ResponseEntity.ok(categoryService.findCategoryListByIds(cids));
    }
}
