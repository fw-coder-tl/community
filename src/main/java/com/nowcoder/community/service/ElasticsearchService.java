package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    public void saveDiscussPost(DiscussPost post){
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        // 使用 NativeQueryBuilder 构建查询条件
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();

        // 构建搜索条件，在 title 和 content 字段中搜索关键词
        queryBuilder.withQuery(q -> q
                .bool(b -> b
                        .should(s -> s
                                .match(m -> m
                                        .field("title")
                                        .query(keyword)
                                )
                        )
                        .should(s -> s
                                .match(m -> m
                                        .field("content")
                                        .query(keyword)
                                )
                        )
                )
        );
        // 设置分页
        queryBuilder.withPageable(PageRequest.of(current, limit));

        // 构建查询
        NativeQuery query = queryBuilder.build();

        // 执行查询
        SearchHits<DiscussPost> searchHits = elasticTemplate.search(query, DiscussPost.class);

        // 处理查询结果
        List<DiscussPost> content = searchHits.stream()
                // 修改处：使用正确的方式获取内容
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(content, PageRequest.of(current, limit), searchHits.getTotalHits());

    }
}
