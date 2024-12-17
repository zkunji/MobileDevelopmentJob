package com.pojos;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Zhangkunji
 * @date 2024/12/11
 * @Description
 */


@Data
@TableName("user")
public class User {
    private String userId = UUID.randomUUID().toString().replace("-", "");
    private String userEmail;
    private String username;
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate = LocalDateTime.now();

    public User(String userEmail, String password) {
        this.userEmail = userEmail;
        this.password = password;
        this.username = userEmail;
    }

    public User() {
    }
}
