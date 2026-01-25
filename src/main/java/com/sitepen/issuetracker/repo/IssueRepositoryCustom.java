package com.sitepen.issuetracker.repo;

import com.sitepen.issuetracker.model.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IssueRepositoryCustom {

    Page<Issue> findWithFilters(String projectId, String status,
                                String priority, String assigneeId,
                                String search, Pageable pageable);

    List<Issue> findByFullTextSearch(String searchText);
}
