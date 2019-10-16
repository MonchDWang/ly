package com.leyou.user.controller;

import com.leyou.common.exception.LyException;
import com.leyou.user.DTO.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.service.TbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private TbUserService userService;
    /**
     * 检查用户信息是否可用
     * type 1- 用户名 2-手机号
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUser(@PathVariable(name = "data") String data,
                                             @PathVariable(name = "type") Integer type){
        return ResponseEntity.ok(userService.checkUser(data,type));
    }

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam(name = "phone") String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid TbUser user, BindingResult result, @RequestParam(name = "code")String code){
        String message = "";
        if(result.hasErrors()){
            List<ObjectError> allErrors = result.getAllErrors();
            for (ObjectError allError : allErrors) {
                if(message.length()>0){
                    message +="|";
                }
                message += allError.getDefaultMessage();
            }
            throw new LyException(400,message);
        }
        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    /**
     * 用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    public ResponseEntity<UserDTO> query(@RequestParam(name = "username")String username,
                                         @RequestParam(name = "password")String password){
        return ResponseEntity.ok(userService.queryUser(username,password));
    }
}
