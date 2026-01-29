package com.issuetracker.service;

import com.issuetracker.dto.response.PageResponse;
import com.issuetracker.dto.response.ProjectResponse;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.Project;
import com.issuetracker.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

        @ExtendWith(MockitoExtension.class)
        public class ProjectServiceTest {

            @Mock
            private ProjectRepository projectRepository;

            @Mock
            private ModelMapper modelMapper;

            @InjectMocks
            private ProjectService projectService;

            @Test
            public void testGetAllProjects_NoProjectsFound_ReturnsEmptyPageResponse() {
                // Arrange
                Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Project> emptyPage = Page.empty(pageable);

                when(projectRepository.findAll(pageable)).thenReturn(emptyPage);

                // Act
                PageResponse<ProjectResponse> response = projectService.getAllProjects(0, 10);

                // Assert
                assertEquals(0, response.getContent().size());
                assertEquals(0, response.getTotalElements());
                assertEquals(0, response.getTotalPages());
                assertEquals(0, response.getPage());
                assertEquals(10, response.getSize());

                verify(projectRepository, times(1)).findAll(pageable);
            }

            @Test
            public void testGetAllProjects_ProjectsFound_ReturnsPageResponseWithData() {
                // Arrange
                Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

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
                Page<Project> projectPage = new PageImpl<>(projects, pageable, projects.size());

                when(projectRepository.findAll(pageable)).thenReturn(projectPage);
                when(modelMapper.map(project1, ProjectResponse.class)).thenReturn(ProjectResponse.builder().name("Project 1").description("Description for Project 1").ownerId("owner1").build());
                when(modelMapper.map(project2, ProjectResponse.class)).thenReturn(ProjectResponse.builder().name("Project 2").description("Description for Project 2").ownerId("owner2").build());


                // Act
                PageResponse<ProjectResponse> response = projectService.getAllProjects(0, 10);

                // Assert
                assertEquals(2, response.getContent().size());
                assertEquals(2, response.getTotalElements());
                assertEquals(1, response.getTotalPages());
                assertEquals(0, response.getPage());
                assertEquals(10, response.getSize());

                ProjectResponse responseProject1 = response.getContent().get(0);
                ProjectResponse responseProject2 = response.getContent().get(1);

                assertEquals("Project 1", responseProject1.getName());
                assertEquals("Description for Project 1", responseProject1.getDescription());
                assertEquals("owner1", responseProject1.getOwnerId());

                assertEquals("Project 2", responseProject2.getName());
                assertEquals("Description for Project 2", responseProject2.getDescription());
                assertEquals("owner2", responseProject2.getOwnerId());

                verify(projectRepository, times(1)).findAll(pageable);
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

                assertEquals("Project not found with id: " + projectId, exception.getMessage());
                verify(projectRepository, times(1)).findById(projectId);
                verifyNoInteractions(modelMapper);
            }
        }
