package com.sitepen.issuetracker.repo;

import com.sitepen.issuetracker.model.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IssueRepository extends MongoRepository<Issue, String>, IssueRepositoryCustom {
    @Query("{ 'projectId': ?0 }")
    Page<Issue> findByProjectId(String projectId, Pageable pageable);

    @Query("{ 'assigneeId': ?0, 'status': { $ne: 'CLOSED' } }")
    List<Issue> findActiveByAssignee(String assigneeId);
}
