package com.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pojos.Income;
import org.springframework.stereotype.Service;

/**
 * @author Zhangkunji
 * @date 2024/12/16
 * @Description
 */
@Service
public interface IncomeService extends IService<Income> {
    SaResult overview(String userId);
}
