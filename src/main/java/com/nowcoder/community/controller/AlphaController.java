package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import jakarta.servlet.http.Cookie;
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

    @RequestMapping(path="/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效的范围
        cookie.setPath("/community/alpha");
        //设置cookie的生存时间
        cookie.setMaxAge(60 * 10);
        //发送cookie
        response.addCookie(cookie);
        return "set cookie";

    }



}
