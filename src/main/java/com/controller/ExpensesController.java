package com.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.pojos.Expenses;
import com.service.ExpensesService;
import org.springframework.web.bind.annotation.*;

/**
 * @author Zhangkunji
 * @date 2024/12/17
 * @Description
 */

@RestController
@RequestMapping("/expenses")
public class ExpensesController {

    private final ExpensesService expensesService;

    public ExpensesController(ExpensesService expensesService) {
        this.expensesService = expensesService;
    }

    @PostMapping("/add")
    public SaResult addRecord(@RequestBody Expenses expenses) {
        return expensesService.addRecord(expenses);
    }

    @GetMapping("/get")
    public SaResult getRecord() {
        String UID = (String) StpUtil.getLoginId();
        return expensesService.getRecord(UID);
    }

    @PostMapping("/delete")
    public SaResult deleteRecord(Integer id) {
        return expensesService.deleteRecord(id);
    }

    //圆环图
    @GetMapping("/getExpenditureDCData")
    public SaResult getCategoryOfConsumption() {
        String UID = (String) StpUtil.getLoginId();
        return expensesService.getCategoryOfConsumption(UID);
    }

    //圆环图
    @GetMapping("/getIncomeDCData")
    public SaResult getCategoryOfIncome() {
        String UID = (String) StpUtil.getLoginId();
        return expensesService.getCategoryOfIncome(UID);
    }

    //折线图
    @GetMapping("/getExpenditureLCData")
    public SaResult getLineChartExpenditureData() {
        String UID = (String) StpUtil.getLoginId();
        return expensesService.getLineChartExpenditureData(UID);
    }

    //折线图
    @GetMapping("/getIncomeLCData")
    public SaResult getLineChartIncomeData() {
        String UID = (String) StpUtil.getLoginId();
        return expensesService.getLineChartIncomeData(UID);
    }

    @GetMapping("/getBarData")
    public SaResult getBarData() {
        String UID = (String) StpUtil.getLoginId();
        return expensesService.getBarChartData(UID);
    }
}
