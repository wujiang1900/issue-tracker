package com.issuetracker.service;

import com.issuetracker.dto.request.CreateProjectRequest;
import com.issuetracker.dto.request.UpdateProjectRequest;
import com.issuetracker.dto.response.PageResponse;
import com.issuetracker.dto.response.ProjectResponse;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.Project;
import com.issuetracker.model.User;
import com.issuetracker.security.UserRole;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.validation.ProjectOwnerValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        String ownerId = request.getOwnerId();
        User owner = new ProjectOwnerValidator(userRepository).validateOwnerId(ownerId);

        if(owner.getRole() != UserRole.PROJECT_OWNER) {
            throw new IllegalArgumentException("Only project owners can own projects");
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(ownerId)
                .ownerName(owner.getName())
                .issueCount(0)
                .build();

        Project savedProject = projectRepository.save(project);
        return modelMapper.map(savedProject, ProjectResponse.class);
    }

    public ProjectResponse getProjectById(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return modelMapper.map(project, ProjectResponse.class);
    }

    public PageResponse<ProjectResponse> getAllProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Project> projectPage = projectRepository.findAll(pageable);

        PageResponse<ProjectResponse> response = new PageResponse<>();
        response.setContent(projectPage.getContent().stream()
                .map(project -> modelMapper.map(project, ProjectResponse.class))
                .toList());
        response.setPage(projectPage.getNumber());
        response.setSize(projectPage.getSize());
        response.setTotalElements(projectPage.getTotalElements());
        response.setTotalPages(projectPage.getTotalPages());

        return response;
    }

    @Transactional
    public ProjectResponse updateProject(String id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (request.getOwnerId() != null) {
            String ownerId = request.getOwnerId();
            new ProjectOwnerValidator(userRepository).validateOwnerId(ownerId);
            project.setOwnerId(ownerId);
        }

        if (request.getName() != null) {
            project.setName(request.getName());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        Project updatedProject = projectRepository.save(project);
        return modelMapper.map(updatedProject, ProjectResponse.class);
    }

    @Transactional
    public void deleteProject(String id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        // Note: In production, consider soft delete or archiving
        // Also consider what happens to associated issues
        projectRepository.deleteById(id);
    }
}
