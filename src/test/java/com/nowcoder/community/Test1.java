package com.nowcoder.community;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class Test1 {
    @Autowired
    private UserService userService;
    @Test
    public void test() {
        // 注册用户
        User user = new User();
        user.setUsername("tianliang");
        user.setPassword("0161131");
        user.setEmail("477591620@qq.com");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setHeaderUrl(String.format("http://image.nowcoder.com/header/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userService.register(user);
    }
}
