package com.issuetracker.repository;

import com.issuetracker.model.Issue;
import com.issuetracker.model.IssuePriority;
import com.issuetracker.model.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends MongoRepository<Issue, String> {

    Page<Issue> findByProjectId(String projectId, Pageable pageable);

    Page<Issue> findByStatus(IssueStatus status, Pageable pageable);

    Page<Issue> findByPriority(IssuePriority priority, Pageable pageable);

    Page<Issue> findByAssigneeId(String assigneeId, Pageable pageable);

    Page<Issue> findByTagsContaining(String tag, Pageable pageable);

    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    Page<Issue> searchByTitle(String searchText, Pageable pageable);

    List<Issue> findByProjectId(String projectId);
}
