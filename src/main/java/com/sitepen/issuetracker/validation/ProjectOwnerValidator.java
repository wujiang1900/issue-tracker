package com.sitepen.issuetracker.validation;

import com.sitepen.issuetracker.exception.ResourceNotFoundException;
import com.sitepen.issuetracker.model.User;
import com.sitepen.issuetracker.repo.UserRepository;
import lombok.AllArgsConstructor;

//@AllArgsConstructor
public record ProjectOwnerValidator(UserRepository userRepository) {
    public User validateOwnerId(String ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Project owner not found"));
    }
}
