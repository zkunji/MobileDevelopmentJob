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

    @GetMapping("/getConsumeData")
    public SaResult getCategoryOfConsumption() {
        String UID = (String) StpUtil.getLoginId();
        return expensesService.getCategoryOfConsumption(UID);
    }

    

    @GetMapping("/getIncomeData")
    public SaResult getCategoryOfIncome() {
        String UID = (String) StpUtil.getLoginId();
        return expensesService.getIncomeData(UID);
    }
}
