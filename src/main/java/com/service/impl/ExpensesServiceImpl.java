package com.service.impl;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mapper.CategoriesMapper;
import com.mapper.ExpensesMapper;
import com.mapper.IncomeMapper;
import com.pojos.Categories;
import com.pojos.Expenses;
import com.pojos.Income;
import com.service.ExpensesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhangkunji
 * @date 2024/12/17
 * @Description
 */

@Service
public class ExpensesServiceImpl extends ServiceImpl<ExpensesMapper, Expenses> implements ExpensesService {

    private final ExpensesMapper expensesMapper;
    private final IncomeMapper incomeMapper;
    private final CategoriesMapper categoriesMapper;

    @Autowired
    public ExpensesServiceImpl(ExpensesMapper expensesMapper
            , IncomeMapper incomeMapper
            , CategoriesMapper categoriesMapper) {
        this.expensesMapper = expensesMapper;
        this.incomeMapper = incomeMapper;
        this.categoriesMapper = categoriesMapper;
    }


    /**
     * @param expenses 前端传递的一条新的消费记录
     * @return 返回插入数据库的结果
     */
    @Override
    public SaResult addRecord(Expenses expenses) {
        if (expenses.getType() == 1) {
            income(expenses.getUserId(), expenses.getAmount());
        } else if (expenses.getType() == 0) {
            expenditure(expenses.getUserId(), expenses.getAmount());
        }
        int insert = expensesMapper.insert(expenses);
        if (insert > 0) {
            return SaResult.ok("[INFO]: 消费记录插入成功");
        }
        return SaResult.error("[ERROR]: 消费记录插入失败");
    }

    /**
     * @param userId 用户的id
     * @return 返回当月该用户的所有消费记录
     */
    @Override
    public SaResult getRecord(String userId) {
        List<Expenses> list = expensesMapper.selectList(searchCurrentMonthData(userId));
        return SaResult.data(list);
    }

    @Override
    public SaResult deleteRecord(Integer id) {
        Expenses expenses = expensesMapper.selectById(id);
        String userId = expenses.getUserId();
        Integer type = expenses.getType();
        Double price = expenses.getAmount();
        int i = expensesMapper.deleteById(id);
        //1代表收入,此时需要减去这笔收入
        //0代表支出，要加上这笔支出
        if (type == 1) {
            return expenditure(userId, price);
        } else if (type == 0) {
            return income(userId, price);
        }
        return null;
    }

    /**
     * @param userId 用户id
     * @return 给扇形图返回消费数据
     */
    @Override
    public SaResult getCategoryOfConsumption(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = searchExpensesData(userId);
        List<Expenses> expenses = expensesMapper.selectList(queryWrapper);
        Map<String, Double> data = getMonthIAndEData(expenses);
        return SaResult.data(data);
    }


    @Override
    public SaResult getCategoryOfIncome(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = searchIncomeData(userId);
        List<Expenses> income = expensesMapper.selectList(queryWrapper);
        Map<String, Double> data = getMonthIAndEData(income);
        return SaResult.data(data);
    }

    /**
     * @param data 消费记录
     * @return 返回一个Map，包含消费类型与各自对应的总金额
     */
    public Map<String, Double> getMonthIAndEData(List<Expenses> data) {
        List<Categories> categories = categoriesMapper.selectList(null);
        Map<Integer, String> categoryMap = new HashMap<>();
        //创建category id与name的映射
        for (Categories c : categories) {
            categoryMap.put(c.getCategoryId(), c.getCategoryName());
        }
        Map<String, Double> map = new HashMap<>();
        for (Expenses e : data) {
            String categoryName = categoryMap.getOrDefault(e.getCategoryId(), "未知");
            map.merge(categoryName, e.getAmount(), Double::sum);
        }
        return map;
    }

    /**
     * @param userId 用户id
     * @return 返回支出折线图
     */
    @Override
    public SaResult getExpenditureData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = searchExpensesData(userId);
        List<Expenses> expenses = expensesMapper.selectList(queryWrapper);
        Map<Integer, Double> dailyExpendDataMap = new HashMap<>();
        for (Expenses e : expenses) {
            if (e.getType() == 0) {
                LocalDateTime time = e.getExpenseDate();
                int dayOfMonth = time.getDayOfMonth();
                Double amount = e.getAmount();
                dailyExpendDataMap.merge(dayOfMonth, amount, Double::sum);
            }
        }
        return SaResult.data(dailyExpendDataMap);
    }

    /**
     * @param userId 用户id
     * @return 返回收入折线图数据
     */
    @Override
    public SaResult getIncomeData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = searchExpensesData(userId);
        List<Expenses> income = expensesMapper.selectList(queryWrapper);
        Map<Integer, Double> dailyIncomeDataMap = new HashMap<>();
        for (Expenses e : income) {
            if (e.getType() == 1) {
                LocalDateTime time = e.getExpenseDate();
                int dayOfMonth = time.getDayOfMonth();
                Double amount = e.getAmount();
                dailyIncomeDataMap.merge(dayOfMonth, amount, Double::sum);
            }
        }
        return SaResult.data(dailyIncomeDataMap);
    }

    public SaResult expenditure(String userId, Double price) {
        LambdaUpdateWrapper<Income> updateWrapper = searchData(userId);
        Income data = incomeMapper.selectOne(updateWrapper);
        if (data != null) {
            Double currentExpenditure = data.getExpenditure();
            Double currentSurplus = data.getSurplus();
            Double newExpenditure = currentExpenditure + price;
            Double newSurplus = currentSurplus - price;
            //更新结余和支出信息
            updateWrapper.set(Income::getExpenditure, newExpenditure)
                    .set(Income::getSurplus, newSurplus);
            int update = incomeMapper.update(updateWrapper);
            if (!(update > 0)) {
                return SaResult.error("[ERROR]: 更新支出信息失败");
            }
            return SaResult.data(newExpenditure);
        }
        return SaResult.error("[ERROR]: 未查询到相关信息");
    }

    public SaResult income(String userId, Double price) {
        LambdaUpdateWrapper<Income> updateWrapper = searchData(userId);
        Income data = incomeMapper.selectOne(updateWrapper);
        if (data != null) {
            Double currentIncome = data.getTotalIncome();
            Double currentSurplus = data.getSurplus();
            Double newIncome = currentIncome + price;
            Double newSurplus = currentSurplus + price;
            updateWrapper.set(Income::getTotalIncome, newIncome)
                    .set(Income::getSurplus, newSurplus);
            int update = incomeMapper.update(updateWrapper);
            if (!(update > 0)) {
                return SaResult.error("[ERROR]: 更新收入信息失败");
            }
            return SaResult.data(newIncome);
        }
        return SaResult.error("[ERROR]: 未查询到相关信息");
    }

    public LambdaUpdateWrapper<Income> searchData(String userId) {
        LambdaUpdateWrapper<Income> updateWrapper = new LambdaUpdateWrapper<>();
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        updateWrapper.between(Income::getMonth, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Income::getUserId, userId));
        return updateWrapper;
    }

    public LambdaQueryWrapper<Expenses> searchCurrentMonthData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = new LambdaQueryWrapper<>();
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        queryWrapper.between(Expenses::getExpenseDate, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Expenses::getUserId, userId));
        return queryWrapper;
    }

    public LambdaQueryWrapper<Expenses> searchExpensesData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = new LambdaQueryWrapper<>();
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        queryWrapper.between(Expenses::getExpenseDate, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Expenses::getUserId, userId).eq(Expenses::getType, 0));
        return queryWrapper;
    }

    public LambdaQueryWrapper<Expenses> searchIncomeData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = new LambdaQueryWrapper<>();
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        queryWrapper.between(Expenses::getExpenseDate, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Expenses::getUserId, userId).eq(Expenses::getType, 1));
        return queryWrapper;
    }
}
