package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.reggie.common.R;
import com.cao.reggie.entity.Employee;
import com.cao.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录功能
     *
     * @param request
     * @param employee
     * @return 登录信息
     */

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee getEmployee = employeeService.getOne(lambdaQueryWrapper);
        //3、如果没有查询到则返回登录失败结果
        if (getEmployee == null) {
            return R.error("用户不存在");
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if (!password.equals(getEmployee.getPassword())) {
            return R.error("密码错误");
        }
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (getEmployee.getStatus() == 0) {
            return R.error("该用户已禁用");
        }
        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", getEmployee.getId());
        return R.success(getEmployee);
    }

    /**
     * 用户退出
     *
     * @param request
     * @return 状态信息
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     *
     * @param request  HTTP请求
     * @param employee 员工数据
     * @return 状态信息
     */
    @PostMapping
    public R<String> add(HttpServletRequest request, @RequestBody Employee employee) {
        //设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前用户的ID
        Long empId = (Long) request.getSession().getAttribute("employee");
        //设置新账号的创建人
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        //保存数据
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 分页查询员工
     *
     * @param page     当前页码
     * @param pageSize 每页几个
     * @param name     模糊查询
     * @return 查询结果
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器
        Page pageInfo = new Page(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getCreateTime);

        //查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 修改员工的状态
     *
     * @param request  请求体
     * @param employee 需要更改的信息
     * @return 更改状态
     */
    @PutMapping
    public R<String> setStatus(HttpServletRequest request, @RequestBody Employee employee) {
        //获取当前修改人
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empId);
        //设置当前修改时间
        employee.setUpdateTime(LocalDateTime.now());
        //更新状态
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
}
