package com.leyou.item.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import com.leyou.item.entity.Item;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class ItemController {

    /**
     * 保存
     * @param item
     */
    @PostMapping("/saveItem")
    public ResponseEntity<Item> saveItem(Item item, HttpServletResponse response){
        if(item.getPrice() == null){
           throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        return ResponseEntity.ok(new Item(1L,"p30",4500L));
    }
}
