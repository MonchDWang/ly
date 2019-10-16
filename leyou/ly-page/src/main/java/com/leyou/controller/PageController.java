package com.leyou.controller;

import com.leyou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;
    @RequestMapping("/hello")
    public String hello(Model model){
        String str =  "hello thymeleaf!!!!";
        model.addAttribute("hello",str);
        return "hello";
    }

    /**
     * 显示模板
     * @param model
     * @param spuId
     * @return
     */
    @RequestMapping("/item/{id}.html")
    public String item(Model model, @PathVariable(name = "id")Long spuId){
        Map<String,Object> map = pageService.loadData(spuId);
        model.addAllAttributes(map);
        return "item";
    }
}
