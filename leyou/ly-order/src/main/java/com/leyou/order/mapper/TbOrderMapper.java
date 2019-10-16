package com.leyou.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.order.entity.TbOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
public interface TbOrderMapper extends BaseMapper<TbOrder> {

    @Select("select order_id from tb_order where create_time <= #{overtime} and status = 1")
    List<Long> selectOverTimeOrderIds(@Param("overtime") Date dateTime);
}
