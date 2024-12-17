package com.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pojos.Expenses;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Zhangkunji
 * @date 2024/12/15
 * @Description
 */

@Mapper
public interface ExpensesMapper extends BaseMapper<Expenses> {
}