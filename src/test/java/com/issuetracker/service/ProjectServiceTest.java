package com.issuetracker.service;

import com.issuetracker.dto.response.ProjectResponse;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.Project;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProjectService projectService;

    @Test
    public void testGetAllProjects_NoProjectsFound_ReturnsEmptyList() {
        // Arrange
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ProjectResponse> response = projectService.getAllProjects();

        // Assert
        assertTrue(response.isEmpty());
        assertEquals(0, response.size());

        verify(projectRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllProjects_ProjectsFound_ReturnsListWithData() {
        // Arrange
        Project project1 = Project.builder()
                .id("project1")
                .name("Project 1")
                .description("Description for Project 1")
                .ownerId("owner1")
                .ownerName("Owner Name 1")
                .issueCount(5)
                .build();

        Project project2 = Project.builder()
                .id("project2")
                .name("Project 2")
                .description("Description for Project 2")
                .ownerId("owner2")
                .ownerName("Owner Name 2")
                .issueCount(3)
                .build();

        List<Project> projects = List.of(project1, project2);

        ProjectResponse projectResponse1 = ProjectResponse.builder()
                .id("project1")
                .name("Project 1")
                .description("Description for Project 1")
                .ownerId("owner1")
                .ownerName("Owner Name 1")
                .issueCount(5)
                .build();

        ProjectResponse projectResponse2 = ProjectResponse.builder()
                .id("project2")
                .name("Project 2")
                .description("Description for Project 2")
                .ownerId("owner2")
                .ownerName("Owner Name 2")
                .issueCount(3)
                .build();

        when(projectRepository.findAll()).thenReturn(projects);
        when(modelMapper.map(project1, ProjectResponse.class)).thenReturn(projectResponse1);
        when(modelMapper.map(project2, ProjectResponse.class)).thenReturn(projectResponse2);

        // Act
        List<ProjectResponse> response = projectService.getAllProjects();

        // Assert
        assertEquals(2, response.size());

        ProjectResponse responseProject1 = response.get(0);
        ProjectResponse responseProject2 = response.get(1);

        assertEquals("Project 1", responseProject1.getName());
        assertEquals("Description for Project 1", responseProject1.getDescription());
        assertEquals("owner1", responseProject1.getOwnerId());

        assertEquals("Project 2", responseProject2.getName());
        assertEquals("Description for Project 2", responseProject2.getDescription());
        assertEquals("owner2", responseProject2.getOwnerId());

        verify(projectRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(project1, ProjectResponse.class);
        verify(modelMapper, times(1)).map(project2, ProjectResponse.class);
    }

    @Test
    public void testGetProjectById_ProjectExists_ReturnsProjectResponse() {
        // Arrange
        String projectId = "project1";
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test Description")
                .ownerId("owner1")
                .ownerName("Test Owner")
                .issueCount(5)
                .build();

        ProjectResponse projectResponse = ProjectResponse.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test Description")
                .ownerId("owner1")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.of(project));
        when(modelMapper.map(project, ProjectResponse.class)).thenReturn(projectResponse);

        // Act
        ProjectResponse response = projectService.getProjectById(projectId);

        // Assert
        assertEquals("Test Project", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertEquals("owner1", response.getOwnerId());

        verify(projectRepository, times(1)).findById(projectId);
        verify(modelMapper, times(1)).map(project, ProjectResponse.class);
    }

    @Test
    public void testGetProjectById_ProjectDoesNotExist_ThrowsResourceNotFoundException() {
        // Arrange
        String projectId = "nonexistentProject";

        when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.getProjectById(projectId)
        );

        assertEquals("Project not found with id: nonexistentProject", exception.getMessage());
        verify(projectRepository, times(1)).findById(projectId);
        verifyNoInteractions(modelMapper);
    }
}
