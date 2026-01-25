package com.sitepen.issuetracker.repo;

import com.sitepen.issuetracker.model.Issue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class IssueRepositoryCustomImpl implements IssueRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Issue> findWithFilters(String projectId, String status,
                                       String priority, String assigneeId,
                                       String search, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (projectId != null) {
            criteriaList.add(Criteria.where("project_id").is(projectId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }
        if (priority != null) {
            criteriaList.add(Criteria.where("priority").is(priority));
        }
        if (assigneeId != null) {
            criteriaList.add(Criteria.where("assignee_id").is(assigneeId));
        }
        if (search != null && !search.isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("title").regex(search, "i"),
                Criteria.where("description").regex(search, "i")
            );
            criteriaList.add(searchCriteria);
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        query.with(pageable);

        long count = mongoTemplate.count(query, Issue.class);
        List<Issue> issues = mongoTemplate.find(query, Issue.class);

        return new PageImpl<>(issues, pageable, count);
    }

    @Override
    public List<Issue> findByFullTextSearch(String searchText) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(searchText);
        Query query = TextQuery.queryText(criteria).sortByScore();
        return mongoTemplate.find(query, Issue.class);
    }
}
