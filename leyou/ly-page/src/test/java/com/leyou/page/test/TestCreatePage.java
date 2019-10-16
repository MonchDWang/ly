package com.leyou.page.test;

import com.leyou.service.PageService;
import net.bytebuddy.asm.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestCreatePage {
    @Autowired
    private PageService pageService;

    @Test
    public void createPage(){
        List<Long> ids = Arrays.asList(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
        for (Long id : ids) {
            pageService.createHtml(id);
        }

    }
}
