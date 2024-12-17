package com.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Zhangkunji
 * @date 2024/12/15
 * @Description 消费类别
 */

@Data
@TableName("categories")
public class Categories {
    @TableId(type = IdType.AUTO)
    private Integer categoryId;
    private String categoryName;

    public Categories(String categoryName) {
        this.categoryName = categoryName;
    }
}
