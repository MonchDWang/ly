package com.leyou.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.order.entity.TbOrder;
import com.leyou.order.mapper.TbOrderMapper;
import com.leyou.order.service.TbOrderService;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
@Service
public class TbOrderServiceImpl extends ServiceImpl<TbOrderMapper, TbOrder> implements TbOrderService {

    @Override
    public List<Long> getOverTimeOrderIds(Date dateTime) {
        return this.getBaseMapper().selectOverTimeOrderIds(dateTime);
    }
}
