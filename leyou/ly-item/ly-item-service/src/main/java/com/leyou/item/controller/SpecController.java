package com.leyou.item.controller;

import com.leyou.item.pojo.DTO.SpecGroupDTO;
import com.leyou.item.pojo.DTO.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SpecController {

    @Autowired
    private SpecService specService;

    @GetMapping("/spec/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupListByCid(@RequestParam(name = "id") Long cid){
            return ResponseEntity.ok(specService.findSpecGroupListByCid(cid));
    }

    /**
     * 获取规格参数的集合
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("/spec/params")
    public ResponseEntity<List<SpecParamDTO>> findSpecParamList(@RequestParam(name = "gid",required = false) Long gid,
                                                                @RequestParam(name = "cid" ,required = false) Long cid,
                                                                @RequestParam(name = "searching",required = false) Boolean searching){
        return ResponseEntity.ok(specService.findSpecParamList(gid,cid,searching));
    }
}
