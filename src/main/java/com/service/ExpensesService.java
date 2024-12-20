package com.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pojos.Expenses;

/**
 * @author Zhangkunji
 * @date 2024/12/17
 * @Description
 */
public interface ExpensesService extends IService<Expenses> {
    SaResult addRecord(Expenses expenses);

    SaResult getRecord(String userId);

    SaResult deleteRecord(Integer id);

    //给扇形图返回数据的
    SaResult getCategoryOfConsumption(String userId);

    SaResult getCategoryOfIncome(String userId);

    SaResult getLineChartExpenditureData(String id);

    SaResult getLineChartIncomeData(String id);

    SaResult getBarChartData(String userId);
}
