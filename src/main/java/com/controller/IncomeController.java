package com.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.service.IncomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Zhangkunji
 * @date 2024/12/17
 * @Description
 */

@RestController
@RequestMapping("/getAllData")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public SaResult overview() {
        String UID = (String) StpUtil.getLoginId();
        return SaResult.data(incomeService.overview(UID));
    }
}
