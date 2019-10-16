package com.leyou.search.test;

import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.DTO.BrandDTO;
import com.leyou.item.pojo.DTO.CategoryDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestFeign {
    @Autowired
    private ItemClient itemClient;

    @Test
    public void getCategoryList(){
        List<Long> cids = Arrays.asList(74L, 75L, 76L);
        List<CategoryDTO> categoryListByIds = itemClient.findCategoryListByIds(cids);
        for (CategoryDTO categoryListById : categoryListByIds) {
            System.out.println(categoryListById.getName());
        }
    }

    @Test
    public void getBrand(){
        BrandDTO brandDTO = itemClient.findBrandById(1115L);
        System.out.println(brandDTO);
    }
}
