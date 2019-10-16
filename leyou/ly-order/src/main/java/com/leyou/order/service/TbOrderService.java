package com.leyou.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.order.entity.TbOrder;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
public interface TbOrderService extends IService<TbOrder> {

    List<Long> getOverTimeOrderIds(Date dateTime);

}
