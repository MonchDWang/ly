package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.DTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private SpringTemplateEngine templateEngine;
    /**
     * 通过spuid 获取 详情页面需要的数据
     * @param spuId
     */
    public Map<String,Object> loadData(Long spuId) {
        Map<String,Object> returnMap = new HashMap<>();
//        1、查询spu的信息
        SpuDTO spuDTO = itemClient.findSpuBySpuId(spuId);
//        categories  当前spuid对应的分类的集合数据
        List<Long> categoryIds = spuDTO.getCategoryIds();
        List<CategoryDTO> categoryDTOList = itemClient.findCategoryListByIds(categoryIds);
//        brand spuid 对应的品牌信息
        BrandDTO brandDTO = itemClient.findBrandById(spuDTO.getBrandId());
//        spuName  spu的名字
        String spuName = spuDTO.getName();
//        subTitle 促销信息
        String subTitle = spuDTO.getSubTitle();
//        detail   spuDetail的数据
        SpuDetailDTO spuDetailDTO = itemClient.findSpuDetailBySpuId(spuId);
//        skus     当前spu 对应的sku的集合
        List<SkuDTO> skuDTOList = itemClient.findSkuListBySpuId(spuId);
//        specs    当前分类的规格组  信息集合
        //item 微服务查询，cid ,[规格参数的分组DTO ，组内的名字[SpecParamDTO] ]  List<SpecGroupDTO>  SepcGroupDTO -> List<SpecParamDTO>
        List<SpecGroupDTO> specGroupDTOList = itemClient.findSpecGroup(spuDTO.getCid3());
        returnMap.put("categories",categoryDTOList);
        returnMap.put("brand",brandDTO);
        returnMap.put("spuName",spuName);
        returnMap.put("subTitle",subTitle);
        returnMap.put("detail",spuDetailDTO);
        returnMap.put("skus",skuDTOList);
        returnMap.put("specs",specGroupDTOList);
        return returnMap;

    }

    /**
     * 创建 静态页面
     */
    public void createHtml(Long spuId){
        //上下文
        Context context = new Context();
        Map<String, Object> returnMap = this.loadData(spuId);
        //设置 动态数据
        context.setVariables(returnMap);

        String filePath = "D:\\coding-software\\nginx-1.12.2\\html\\item";
        File dir = new File(filePath);
        if(!dir.exists()){
            if(!dir.mkdir()){
                throw  new LyException(ExceptionEnum.FILE_WRITER_ERROR);
            }
        }
        File page = new File(dir,spuId+".html");
        PrintWriter printWriter = null;
        try{
            printWriter = new PrintWriter(page);
            templateEngine.process("item",context,printWriter);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            printWriter.close();
        }

    }


    /**
     * 删除静态页面
     * @param spuId
     */
    public void removePage(Long spuId) {
        String filePath = "D:\\coding-software\\nginx-1.12.2\\html\\item";
            File page = new File(filePath,spuId+".html");
            page.delete();
    }
}
