package com.leyou.search.test;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.DTO.SpuDTO;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDB2Es {

    @Autowired
    private SearchService searchService;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private GoodsRepository repository;
    /**
     * 把db的数据导入es中
     */
    @Test
    public void doDb2Es(){

        int page = 1;
        int rows = 100;
        while(true){
            //分页查询spu的信息
            PageResult<SpuDTO> pageResult = itemClient.findSpuByPage(page, rows, null, null);
            if(pageResult == null || CollectionUtils.isEmpty(pageResult.getItems())){
                break;
            }
            List<SpuDTO> spuDTOList = pageResult.getItems();
//            创建list集合 ，写入es库
            List<Goods> goodsList = new ArrayList<>();
            for (SpuDTO spuDTO : spuDTOList) {
                //调用方法，生成goods对象
                Goods goods = searchService.createGoods(spuDTO);
//                把goods对象放入集合 ，等待写入es库
                goodsList.add(goods);
            }
            //要把goodsList 批量保存到es中
            repository.saveAll(goodsList);
            if(spuDTOList.size()<rows){
                //当前页的返回条数不满 100条，说明没有下一页了，退出循环
                break;
            }
            page ++;
        }
    }
}
