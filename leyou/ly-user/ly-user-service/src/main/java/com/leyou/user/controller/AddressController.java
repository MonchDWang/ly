package com.leyou.user.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.user.DTO.AddressDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户地址信息
 * 查询，增加 修改 删除
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    /**
     * 获取用户收货信息
     * @param addressId
     * @return
     */
    @GetMapping
    public ResponseEntity<AddressDTO> findAddress(@RequestParam(name = "id") Long addressId){
        //从ThreadLocal中获取用户id
        Long userId = UserHolder.getUser();
        System.out.println("userId=="+userId);
        if(userId == null || userId ==0){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        AddressDTO address = new AddressDTO();
        address.setUserId(userId);
        address.setId(addressId);
        address.setStreet("顺义区马坡 传智播客");
        address.setCity("北京");
        address.setDistrict("顺义区");
        address.setAddressee("马坡");
        address.setPhone("15800000000");
        address.setProvince("北京");
        address.setPostcode("010000");
        address.setIsDefault(true);
        return ResponseEntity.ok(address);
    }
}
