package com.issuetracker.service;

import com.issuetracker.dto.request.ProjectRequest;
import com.issuetracker.dto.response.ProjectResponse;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.exception.UnauthorizedException;
import com.issuetracker.model.Project;
import com.issuetracker.model.User;
import com.issuetracker.security.UserRole;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Cacheable(value = "projects")
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getProjectById(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return convertToResponse(project);
    }

    public List<ProjectResponse> getProjectsByOwner(String ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse createProject(ProjectRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(user.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        project = projectRepository.save(project);

        // Update user role to PROJECT_OWNER if not already
        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.PROJECT_OWNER);
            userRepository.save(user);
        }

        return convertToResponse(project);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse updateProject(String id, ProjectRequest request, String userEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!project.getOwnerId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You are not authorized to update this project");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setUpdatedAt(LocalDateTime.now());

        project = projectRepository.save(project);
        return convertToResponse(project);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public void deleteProject(String id, String userEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!project.getOwnerId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You are not authorized to delete this project");
        }

        projectRepository.deleteById(id);
    }

    private ProjectResponse convertToResponse(Project project) {
        ProjectResponse response = modelMapper.map(project, ProjectResponse.class);

        // Fetch owner name
        userRepository.findById(project.getOwnerId()).ifPresent(owner ->
            response.setOwnerName(owner.getName())
        );

        return response;
    }
}
