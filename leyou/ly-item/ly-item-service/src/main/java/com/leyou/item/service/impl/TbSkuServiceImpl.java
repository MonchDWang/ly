package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.entity.TbSku;
import com.leyou.item.mapper.TbSkuMapper;
import com.leyou.item.service.TbSkuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * sku表,该表表示具体的商品实体,如黑色的 64g的iphone 8 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
@Service
public class TbSkuServiceImpl extends ServiceImpl<TbSkuMapper, TbSku> implements TbSkuService {

    /**
     * map的数据
     * key -skuid
     * value -num
     * 需要更新多条
     * @param map
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockMinus(Map<Long, Integer> map) {
        Set<Long> skuSet = map.keySet();
        for (Long skuId : skuSet) {
            Integer num = map.get(skuId);
            int code = this.getBaseMapper().minusStock(skuId,num);
            if(code <1){
                throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockPlus(Map<Long, Integer> skuMap) {
        Set<Long> skuIds = skuMap.keySet();
        for (Long skuId : skuIds) {
            Integer num = skuMap.get(skuId);
            int code = this.getBaseMapper().plusStock(skuId,num);
            if(code <1){
                throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
            }
        }
    }
}
