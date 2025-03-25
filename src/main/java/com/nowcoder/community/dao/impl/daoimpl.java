package com.nowcoder.community.dao.impl;

import com.nowcoder.community.dao.dao;
import org.springframework.stereotype.Repository;

@Repository
public class daoimpl implements dao {
    @Override
    public String select() {
        return "hi";
    }
}
