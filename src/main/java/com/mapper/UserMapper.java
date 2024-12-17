package com.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pojos.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Zhangkunji
 * @date 2024/12/11
 * @Description
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
