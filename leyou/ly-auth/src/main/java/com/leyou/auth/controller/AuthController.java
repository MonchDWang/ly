package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录操作
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam(name = "username")String username,
                                      @RequestParam(name = "password")String password,
                                      HttpServletResponse response){
        authService.login(username,password,response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 验证用户是否登录
     * @param request
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest request,HttpServletResponse response){
        return ResponseEntity.ok(authService.verify(request,response));
    }

    /**
     * 用户登出
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(HttpServletRequest request,HttpServletResponse response){
        authService.logout(request,response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
