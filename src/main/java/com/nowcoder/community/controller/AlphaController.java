package com.nowcoder.community.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        System.out.println(request.getHeader("User-Agent"));
        System.out.println(request.getParameter("code"));
        //返回响应数据
        response.setStatus(302);
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        System.out.println(request.getHeader("User-Agent"));
        System.out.println(request.getParameter("code"));
    }

    @RequestMapping(path="/students", method = RequestMethod.POST)
    @ResponseBody
    public String getStudents(String name,int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }






}
