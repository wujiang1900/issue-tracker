package com.sitepen.issuetracker.repo;

import com.sitepen.issuetracker.model.Project;
import com.sitepen.issuetracker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
}
