package com.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mapper.UserMapper;
import com.pojos.User;
import com.service.UserService;
import com.util.BCryptUtil;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @author Zhangkunji
 * @date 2024/12/11
 * @Description
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    @Override
    public SaResult login(String userEmail, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserEmail, userEmail);
        User user = null;
        try {
            user = userMapper.selectList(queryWrapper).get(0);
        } catch (Exception e) {
            return SaResult.error("[ERROR]: 用户名或密码错误");
        }
        String encryptedPassword = user.getPassword();
        boolean isRight = passwordValidation(password, encryptedPassword);
        if (isRight) {
            StpUtil.login(user.getUserId(), new SaLoginModel().setTimeout(60 * 60 * 24 * 30));
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return SaResult.data(tokenInfo);
        }
        return SaResult.error("[ERROR]: 用户名或密码错误");
    }

    @Override
    public SaResult registration(String userEmail, String password) {
        if (userEmail == null || password == null) {
            return SaResult.error("[ERROR]: 注册失败,用户名或密码不能为空");
        }
        EmailValidator emailValidator = EmailValidator.getInstance();
        boolean isValid = emailValidator.isValid(userEmail);
        if (!isValid) {
            return SaResult.error("[ERROR]: 注册失败，邮箱地址非法");
        }
        String encryptedPassword = BCryptUtil.hashPassword(password);
        User newUser = new User(userEmail, encryptedPassword);
        int insertResult = userMapper.insert(newUser);
        if (insertResult > 0) {
            return SaResult.ok("[INFO]: 注册成功");
        }
        return SaResult.error("[ERROR]: 注册失败");
    }

    @Override
    public SaResult rename(String userId, String newUsername) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId);
        updateWrapper.set(User::getUsername, newUsername);
        boolean update = userMapper.update(updateWrapper) > 0;
        if (update) {
            return SaResult.data(newUsername);
        }
        return SaResult.error("[ERROR]: 用户名更新失败");
    }

    @Override
    public SaResult updatePassword(String userId, Map<String, String> param) {
        String oldPassword = param.get("oldPassword");
        String newPassword = param.get("newPassword");
        String reNewPassword = param.get("reNewPassword");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectList(queryWrapper).get(0);
        boolean check = passwordValidation(oldPassword, user.getPassword());
        if (check && Objects.equals(newPassword, reNewPassword)) {
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getUserId, userId);
            updateWrapper.set(User::getPassword, BCryptUtil.hashPassword(newPassword));
            boolean b = userMapper.update(updateWrapper) > 0;
            if (b) {
                return SaResult.ok("[INFO]: 密码更新成功");
            }
        }
        return SaResult.error("[ERROR]: 密码更新失败");
    }

    @Override
    public SaResult updateEmailAddress(String userId, String newEmailAddress) {
        boolean valid = EmailValidator.getInstance().isValid(newEmailAddress);
        if (!valid) {
            return SaResult.error("[ERROR]: 邮箱地址非法");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId);
        updateWrapper.set(User::getUserEmail, newEmailAddress);
        boolean b = userMapper.update(updateWrapper) > 0;
        if (b) {
            return SaResult.data(newEmailAddress);
        }
        return SaResult.error("[ERROR]: 邮箱地址更新失败");
    }

    public boolean passwordValidation(String inputPassword, String encryptedPassword) {
        return BCryptUtil.verifyPassword(inputPassword, encryptedPassword);
    }
}
