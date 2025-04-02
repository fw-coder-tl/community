package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Test
    public void testSensitiveFilter(){
        String text="这里可以赌博，也可以嫖娼，还可以吸毒，但是不能开票，你可以开票吗？";
        text=sensitiveFilter.filter(text);
        System.out.println(text);
        text="这里可以☆赌☆博，也可以☆嫖☆娼☆，还可以☆吸☆毒☆，但是不能☆开☆票☆，你可以☆开☆票☆吗？";
        text=sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
