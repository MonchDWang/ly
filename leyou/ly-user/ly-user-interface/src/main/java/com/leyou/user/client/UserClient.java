package com.leyou.user.client;

import com.leyou.user.DTO.AddressDTO;
import com.leyou.user.DTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {
    /**
     * 用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    UserDTO query(@RequestParam(name = "username")String username,
                                         @RequestParam(name = "password")String password);


    /**
     * 获取用户收货信息
     * @param addressId
     * @return
     */
    @GetMapping("/address")
    AddressDTO findAddress(@RequestParam(name = "id") Long addressId);
}
