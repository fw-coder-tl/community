package com.nowcoder.community.service;

import com.nowcoder.community.controller.LoginController;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.sql.SQLOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    //日志技术
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    /*@Autowired
    private LoginTicketMapper loginTicketMapper;*/

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        //return userMapper.selectById(id);
        User user=getCache(id);
        if(user==null){
            user=initCache(id);
        }
        return user;
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        // 空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u!= null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setHeaderUrl(String.format("http://image.nowcoder.com/header/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //激活码
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    // 激活用户
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }
    // 登录
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        // 空值处理
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        // 验证账号
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        // 验证状态
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        // 验证密码
        password = CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }
        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        //loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey= RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    //退出登录
    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket,1);
        String redisKey= RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    //根据凭证查询用户
    public LoginTicket findLoginTicket(String ticket){
        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    //更新头像
    public int updateHeader(int userId,String headerUrl){
        //return userMapper.updateHeader(userId,headerUrl);
        int rows=userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    //优先从缓存中取值
    private User getCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //取不到缓存是初始化缓存数据
    private User initCache(int userId){
        User user=userMapper.selectById(userId);
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    //数据变更时清除缓存数据
    private void clearCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
