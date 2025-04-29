package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
    }

    @Test
    public void testSearchRepository(){
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();

        // 构建搜索条件，在 title 和 content 字段中搜索关键词
        queryBuilder.withQuery(q -> q
                .bool(b -> b
                        .should(s -> s
                                .match(m -> m
                                        .field("title")
                                        .query("互联网寒冬")
                                )
                        )
                        .should(s -> s
                                .match(m -> m
                                        .field("content")
                                        .query("互联网寒冬")
                                )
                        )
                )
        );
        // 设置分页
        queryBuilder.withPageable(PageRequest.of(1, 10));

        // 构建查询
        NativeQuery query = queryBuilder.build();

        // 执行查询
        SearchHits<DiscussPost> searchHits = elasticTemplate.search(query, DiscussPost.class);

        // 处理查询结果
        List<DiscussPost> content = searchHits.stream()
                // 修改处：使用正确的方式获取内容
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .collect(Collectors.toList());

        for (DiscussPost post : content) {
            System.out.println(post);
        }

    }

}
