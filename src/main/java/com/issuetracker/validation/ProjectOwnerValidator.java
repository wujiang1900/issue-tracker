package com.issuetracker.validation;

import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.User;
import com.issuetracker.repository.UserRepository;

//@AllArgsConstructor
public record ProjectOwnerValidator(UserRepository userRepository) {
    public User validateOwnerId(String ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Project owner not found"));
    }
}
