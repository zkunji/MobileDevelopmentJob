package com.pojos;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Zhangkunji
 * @date 2024/12/15
 * @Description 收入与支出
 */
@Data
@TableName("income")
public class Income {
    private String userId;
    private Double totalIncome;//收入
    private Double expenditure;//支出
    private Double surplus;//结余
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate month = LocalDate.now();

    public Income(String userId, Double totalIncome, Double expenditure, Double surplus) {
        this.userId = userId;
        this.totalIncome = totalIncome;
        this.expenditure = expenditure;
        this.surplus = surplus;
    }

    public Income() {
    }
}
