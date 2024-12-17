package com.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pojos.User;

import java.util.Map;

/**
 * @author Zhangkunji
 * @date 2024/12/11
 * @Description
 */
public interface UserService extends IService<User> {
    SaResult login(String userEmail, String password);

    SaResult registration(String userEmail, String password);

    SaResult rename(String userId, String newUsername);

    SaResult updatePassword(String userId, Map<String, String> param);

    SaResult updateEmailAddress(String userId, String newEmailAddress);
}
