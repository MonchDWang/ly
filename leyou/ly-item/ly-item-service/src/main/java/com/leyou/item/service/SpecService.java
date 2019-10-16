package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.pojo.DTO.SpecGroupDTO;
import com.leyou.item.pojo.DTO.SpecParamDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SpecService {

    @Autowired
    private TbSpecGroupService groupService;
    @Autowired
    private TbSpecParamService paramService;

    public List<SpecGroupDTO> findSpecGroupListByCid(Long cid) {
        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecGroup::getCid,cid);
        List<TbSpecGroup> tbSpecGroupList = groupService.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbSpecGroupList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(tbSpecGroupList,SpecGroupDTO.class);
    }

    /**
     * 根据条件查询规格参数
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    public List<SpecParamDTO> findSpecParamList(Long gid,Long cid,Boolean searching) {
        QueryWrapper<TbSpecParam> queryWrapper = new QueryWrapper<>();
        if(gid != null ){
            queryWrapper.lambda().eq(TbSpecParam::getGroupId,gid);
        }
        if(cid != null){
            queryWrapper.lambda().eq(TbSpecParam::getCid,cid);
        }
        if(searching != null){
            queryWrapper.lambda().eq(TbSpecParam::getSearching,searching);
        }
        List<TbSpecParam> specParamList = paramService.list(queryWrapper);

        if(CollectionUtils.isEmpty(specParamList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(specParamList,SpecParamDTO.class);
    }
}
