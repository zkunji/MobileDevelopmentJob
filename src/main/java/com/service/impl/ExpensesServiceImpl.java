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

import java.time.LocalDate;
import java.util.*;

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
        LambdaQueryWrapper<Income> queryWrapper = searchData(expenses.getUserId());
        List<Income> incomeList = incomeMapper.selectList(queryWrapper);
        Income income = incomeList.isEmpty() ? null : incomeList.get(0);
        if (income == null) {
            Income i = new Income(
                    expenses.getUserId(),
                    0.00,
                    0.00,
                    0.00
            );
            incomeMapper.insert(i);
        }
        LambdaUpdateWrapper<Income> updateWrapper = updateData(expenses.getUserId());
        if (expenses.getType() == 1) {
            if (income != null) {
                updateWrapper
                        .set(Income::getTotalIncome, income.getTotalIncome() + expenses.getAmount())
                        .set(Income::getSurplus, income.getSurplus() + expenses.getAmount());
            }
        } else if (expenses.getType() == 0) {
            if (income != null) {
                updateWrapper
                        .set(Income::getExpenditure, income.getExpenditure() + expenses.getAmount())
                        .set(Income::getSurplus, income.getSurplus() - expenses.getAmount());
            }
        }
        incomeMapper.update(updateWrapper);
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
        System.out.println(type);
        Double price = expenses.getAmount();
        //1代表收入,此时需要减去这笔收入
        //0代表支出，要加上这笔支出
        if (type == 1) {
            if (minusIncome(userId, price)) {
                expensesMapper.deleteById(id);
                LambdaQueryWrapper<Income> queryWrapper = searchData(userId);
                return SaResult.data(incomeMapper.selectList(queryWrapper));
            }
        } else if (type == 0) {
            if (minusExpenditure(userId, price)) {
                expensesMapper.deleteById(id);
                LambdaQueryWrapper<Income> queryWrapper = searchData(userId);
                return SaResult.data(incomeMapper.selectList(queryWrapper));
            }
        }
        return SaResult.error("[ERROR]: 删除消费记录成功");
    }

    /**
     * @param userId 用户id
     * @return 给圆环图返回消费数据
     */
    @Override
    public SaResult getCategoryOfConsumption(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = searchExpensesData(userId);
        List<Expenses> expenses = expensesMapper.selectList(queryWrapper);
        Map<String, Double> data = getMonthIAndEData(expenses);
        return SaResult.data(data);
    }

    /**
     * @param userId 用户Id
     * @return 给圆环图返回收入数据
     */

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
        Map<Integer, String> categoryMap = new TreeMap<>();
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
    public SaResult getLineChartExpenditureData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = searchExpensesData(userId);
        List<Expenses> expenses = expensesMapper.selectList(queryWrapper);
        Map<Integer, Double> dailyExpendDataMap = new TreeMap<>();
        for (Expenses e : expenses) {
            if (e.getType() == 0) {
                LocalDate time = e.getExpenseDate();
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
    public SaResult getLineChartIncomeData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = searchIncomeData(userId);
        List<Expenses> income = expensesMapper.selectList(queryWrapper);
        System.out.println("Income data: " + income);
        Map<Integer, Double> dailyIncomeDataMap = new TreeMap<>();
        for (Expenses e : income) {
            if (e.getType() == 1) {
                LocalDate time = e.getExpenseDate();
                int dayOfMonth = time.getDayOfMonth();
                Double amount = e.getAmount();
                dailyIncomeDataMap.merge(dayOfMonth, amount, Double::sum);
            }
        }
        return SaResult.data(dailyIncomeDataMap);
    }

    @Override
    public SaResult getBarChartData(String userId) {
        LambdaQueryWrapper<Income> queryWrapper = new LambdaQueryWrapper<>();
        List<Income> checkList = incomeMapper.selectList(queryWrapper);
        Map<Integer, Map<String, Double>> monthlyData = new TreeMap<>();
        for (Income i : checkList) {
            LocalDate time = i.getMonth();
            Map<String, Double> dataOfPrice = new LinkedHashMap<>();
            dataOfPrice.put("Income", i.getTotalIncome());
            dataOfPrice.put("expenditure", i.getExpenditure());
            int monthOfYear = time.getMonthValue();
            monthlyData.put(monthOfYear, dataOfPrice);
        }

        return SaResult.data(monthlyData);
    }

    /**
     * 总支出信息
     *
     * @param userId 用户id
     * @param price  消费金额
     * @return 更新的支出信息是否成功
     */
    public boolean minusIncome(String userId, Double price) {
        LambdaQueryWrapper<Income> queryWrapper = searchData(userId);
        LambdaUpdateWrapper<Income> updateWrapper = updateData(userId);
        Income data = incomeMapper.selectOne(queryWrapper);
        if (data != null) {
            Double currentIncome = data.getTotalIncome();
            Double currentSurplus = data.getSurplus();
            Double newIncome = currentIncome - price;
            Double newSurplus = currentSurplus - price;
            //更新结余和支出信息
            updateWrapper.set(Income::getExpenditure, newIncome)
                    .set(Income::getSurplus, newSurplus);
            int update = incomeMapper.update(updateWrapper);
            return update > 0;
        } else {
            Income income = new Income(
                    userId,
                    0.00,
                    price,
                    (-1) * price
            );
            int insert = incomeMapper.insert(income);
            return insert > 0;
        }
    }

    /**
     * 总收入信息
     *
     * @param userId 用户Id
     * @param price  收入金额
     * @return 返回更新状态
     */
    public boolean minusExpenditure(String userId, Double price) {
        LambdaQueryWrapper<Income> queryWrapper = searchData(userId);
        LambdaUpdateWrapper<Income> updateWrapper = updateData(userId);
        Income data = incomeMapper.selectOne(queryWrapper);
        if (data != null) {
            Double currentExpenditure = data.getExpenditure();
            Double currentSurplus = data.getSurplus();
            Double newExpenditure = currentExpenditure - price;
            Double newSurplus = currentSurplus + price;
            updateWrapper.set(Income::getTotalIncome, newExpenditure)
                    .set(Income::getSurplus, newSurplus);
            int update = incomeMapper.update(updateWrapper);
            return update > 0;
        } else {
            Income income = new Income(
                    userId,
                    price,
                    0.00,
                    price
            );
            int insert = incomeMapper.insert(income);
            return insert > 0;
        }
    }


    public LambdaQueryWrapper<Income> searchData(String userId) {
        LambdaQueryWrapper<Income> updateWrapper = new LambdaQueryWrapper<>();
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        updateWrapper.between(Income::getMonth, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Income::getUserId, userId));
        return updateWrapper;
    }

    public LambdaUpdateWrapper<Income> updateData(String userId) {
        LambdaUpdateWrapper<Income> updateWrapper = new LambdaUpdateWrapper<>();
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        updateWrapper.between(Income::getMonth, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Income::getUserId, userId));
        return updateWrapper;
    }

    public LambdaQueryWrapper<Income> searchCurrentYearOfDay(String userId) {
        LambdaQueryWrapper<Income> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        LocalDate lastDayOfYear = firstDayOfYear.plusYears(1).minusDays(1);
        queryWrapper.between(Income::getMonth, firstDayOfYear, lastDayOfYear)
                .and(i -> i.eq(Income::getUserId, userId));
        return queryWrapper;
    }

    public LambdaQueryWrapper<Expenses> searchCurrentMonthData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        queryWrapper.between(Expenses::getExpenseDate, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Expenses::getUserId, userId));
        return queryWrapper;
    }



    public LambdaQueryWrapper<Expenses> searchExpensesData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        queryWrapper.between(Expenses::getExpenseDate, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Expenses::getUserId, userId).eq(Expenses::getType, 0));
        return queryWrapper;
    }

    public LambdaQueryWrapper<Expenses> searchIncomeData(String userId) {
        LambdaQueryWrapper<Expenses> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        queryWrapper.between(Expenses::getExpenseDate, firstDayOfMonth, lastDayOfMonth)
                .and(i -> i.eq(Expenses::getUserId, userId).eq(Expenses::getType, 1));
        return queryWrapper;
    }
}
