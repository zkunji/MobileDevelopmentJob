package com.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.service.UserService;
import com.util.TokenParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Zhangkunji
 * @date 2024/12/16
 * @Description
 */

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public SaResult login(String userEmail, String password) {
        return userService.login(userEmail, password);
    }

    @PostMapping("/registration")
    public SaResult registration(String userEmail, String password) {
        return userService.registration(userEmail, password);
    }

    @PostMapping("/rename")
    public SaResult rename(String newUserName) {
        String UID = (String) StpUtil.getLoginId();
        return userService.rename(UID, newUserName);
    }

    @PostMapping("/resetPassword")
    public SaResult updatePassword(@RequestBody Map<String, String> param) {
        String UID = (String) StpUtil.getLoginId();
        return userService.updatePassword(UID, param);
    }

    @PostMapping("/updateEmail")
    public SaResult updateEmailAddress(String newEmailAddress) {
        String UID = (String) StpUtil.getLoginId();
        return userService.updateEmailAddress(UID, newEmailAddress);
    }

}
