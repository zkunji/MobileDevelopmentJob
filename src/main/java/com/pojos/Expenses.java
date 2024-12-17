package com.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Zhangkunji
 * @date 2024/12/15
 * @Description 消费记录
 */

@Data
@TableName("expenses")
public class Expenses {
    @TableId(type = IdType.AUTO)
    private Integer expensesId;
    private String userId;
    private Integer categoryId;
    private Double amount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expenseDate = LocalDateTime.now();
    private Integer type;
    private String note;

    public Expenses(Double amount, Integer category_id, String user_id, Integer type, String note) {
        this.amount = amount;
        this.categoryId = category_id;
        this.userId = user_id;
        this.type = type;
        this.note = note;
    }
}
