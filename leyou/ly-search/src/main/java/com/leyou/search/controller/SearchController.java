package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.DTO.GoodsDTO;
import com.leyou.search.DTO.SearchRequest;
import com.leyou.search.service.SearchService;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 用户输入关键词，进行搜索
     * @param request
     * @return
     */
    @PostMapping("/page")
    public ResponseEntity<PageResult<GoodsDTO>> search(@RequestBody  SearchRequest request){
            return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * 查询过滤条件
     * @param request
     * @return
     */
    @PostMapping("/filter")
    public ResponseEntity<Map<String, List<?>>> searchFilter(@RequestBody SearchRequest request){
        return ResponseEntity.ok(searchService.searchFilter(request));
    }
}
