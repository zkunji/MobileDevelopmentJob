package com.service.impl;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mapper.IncomeMapper;
import com.pojos.Income;
import com.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author Zhangkunji
 * @date 2024/12/16
 * @Description
 */

@Service
public class IncomeServiceImpl extends ServiceImpl<IncomeMapper, Income> implements IncomeService {

    private final IncomeMapper incomeMapper;

    @Autowired
    public IncomeServiceImpl(IncomeMapper incomeMapper) {
        this.incomeMapper = incomeMapper;
    }

    @Override
    public SaResult overview(String userId) {
        LambdaQueryWrapper<Income> queryWrapper = searchCurrentMonthData(userId);
        Income income1 = incomeMapper.selectList(queryWrapper).get(0);
        if (income1==null){
            Income defaultData = new Income(
                    userId,
                    0.00,
                    0.00,
                    0.00
            );
            incomeMapper.insert(defaultData);
            return SaResult.data(defaultData);
        }
        return SaResult.data(income1);
    }
    public LambdaQueryWrapper<Income> searchCurrentMonthData(String userId) {
        LambdaQueryWrapper<Income> queryWrapper = new LambdaQueryWrapper<>();
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        queryWrapper.between(Income::getMonth, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Income::getUserId, userId));
        return queryWrapper;
    }
}
