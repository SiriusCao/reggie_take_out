package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cao.reggie.common.R;
import com.cao.reggie.entity.Employee;
import com.cao.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录功能
     * @param request
     * @param employee
     * @return 登录信息
     */

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper= new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee getEmployee = employeeService.getOne(lambdaQueryWrapper);
        //3、如果没有查询到则返回登录失败结果
        if (getEmployee==null){
            return R.error("用户不存在");
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if (!password.equals(getEmployee.getPassword())){
            return R.error("密码错误");
        }
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (getEmployee.getStatus()==0){
            return R.error("该用户已禁用");
        }
        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",getEmployee.getId());
        return R.success(getEmployee);
    }

    /**
     * 用户退出
     * @param request
     * @return 状态信息
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
}
