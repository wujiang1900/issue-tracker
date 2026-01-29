package com.issuetracker.config;

import com.issuetracker.model.*;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                    ProjectRepository projectRepository,
                                    IssueRepository issueRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if data already exists
            if (userRepository.count() > 0) {
                log.info("Database already seeded, skipping...");
                return;
            }

            log.info("Seeding database with initial data...");

            // Create users
            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@issuetracker.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            admin = userRepository.save(admin);

            User projectOwner = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(UserRole.PROJECT_OWNER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            projectOwner = userRepository.save(projectOwner);

            User developer1 = User.builder()
                    .name("Jane Smith")
                    .email("jane@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(UserRole.USER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            developer1 = userRepository.save(developer1);

            User developer2 = User.builder()
                    .name("Bob Wilson")
                    .email("bob@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(UserRole.USER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            developer2 = userRepository.save(developer2);

            log.info("Created {} users", userRepository.count());

            // Create projects
            Project project1 = Project.builder()
                    .name("Issue Tracker Application")
                    .description("A full-stack issue tracking system with real-time updates")
                    .ownerId(projectOwner.getId())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            project1 = projectRepository.save(project1);

            Project project2 = Project.builder()
                    .name("E-Commerce Platform")
                    .description("Online shopping platform with payment integration")
                    .ownerId(projectOwner.getId())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            project2 = projectRepository.save(project2);

            log.info("Created {} projects", projectRepository.count());

            // Create issues for project 1
            Issue issue1 = Issue.builder()
                    .title("Implement user authentication")
                    .description("Add JWT-based authentication with email and password")
                    .status(IssueStatus.CLOSED)
                    .priority(IssuePriority.HIGH)
                    .projectId(project1.getId())
                    .assigneeId(developer1.getId())
                    .tags(Arrays.asList("backend", "security", "authentication"))
                    .comments(new ArrayList<>())
                    .activityLogs(Arrays.asList(
                            ActivityLog.builder()
                                    .userId(projectOwner.getId())
                                    .userName(projectOwner.getName())
                                    .action("CREATED")
                                    .details("Issue created")
                                    .timestamp(LocalDateTime.now().minusDays(5))
                                    .build(),
                            ActivityLog.builder()
                                    .userId(developer1.getId())
                                    .userName(developer1.getName())
                                    .action("UPDATED")
                                    .details("Status changed from OPEN to IN_PROGRESS")
                                    .timestamp(LocalDateTime.now().minusDays(4))
                                    .build(),
                            ActivityLog.builder()
                                    .userId(developer1.getId())
                                    .userName(developer1.getName())
                                    .action("UPDATED")
                                    .details("Status changed from IN_PROGRESS to CLOSED")
                                    .timestamp(LocalDateTime.now().minusDays(2))
                                    .build()
                    ))
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .updatedAt(LocalDateTime.now().minusDays(2))
                    .build();

            issue1.getComments().add(Comment.builder()
                    .id("comment1")
                    .userId(developer1.getId())
                    .userName(developer1.getName())
                    .text("I've implemented the JWT authentication. Ready for review.")
                    .timestamp(LocalDateTime.now().minusDays(2))
                    .build());

            issueRepository.save(issue1);

            Issue issue2 = Issue.builder()
                    .title("Add WebSocket support for real-time updates")
                    .description("Implement WebSocket connections to push real-time issue updates to clients")
                    .status(IssueStatus.IN_PROGRESS)
                    .priority(IssuePriority.HIGH)
                    .projectId(project1.getId())
                    .assigneeId(developer2.getId())
                    .tags(Arrays.asList("backend", "websocket", "real-time"))
                    .comments(new ArrayList<>())
                    .activityLogs(Arrays.asList(
                            ActivityLog.builder()
                                    .userId(projectOwner.getId())
                                    .userName(projectOwner.getName())
                                    .action("CREATED")
                                    .details("Issue created")
                                    .timestamp(LocalDateTime.now().minusDays(3))
                                    .build(),
                            ActivityLog.builder()
                                    .userId(developer2.getId())
                                    .userName(developer2.getName())
                                    .action("UPDATED")
                                    .details("Status changed from OPEN to IN_PROGRESS")
                                    .timestamp(LocalDateTime.now().minusDays(2))
                                    .build()
                    ))
                    .createdAt(LocalDateTime.now().minusDays(3))
                    .updatedAt(LocalDateTime.now().minusDays(2))
                    .build();

            issue2.getComments().add(Comment.builder()
                    .id("comment2")
                    .userId(developer2.getId())
                    .userName(developer2.getName())
                    .text("Working on the STOMP configuration")
                    .timestamp(LocalDateTime.now().minusDays(1))
                    .build());

            issueRepository.save(issue2);

            Issue issue3 = Issue.builder()
                    .title("Design MongoDB schema with proper indexing")
                    .description("Create efficient MongoDB schema with compound indexes for filtering")
                    .status(IssueStatus.CLOSED)
                    .priority(IssuePriority.MEDIUM)
                    .projectId(project1.getId())
                    .assigneeId(developer1.getId())
                    .tags(Arrays.asList("database", "mongodb", "performance"))
                    .comments(new ArrayList<>())
                    .activityLogs(Arrays.asList(
                            ActivityLog.builder()
                                    .userId(projectOwner.getId())
                                    .userName(projectOwner.getName())
                                    .action("CREATED")
                                    .details("Issue created")
                                    .timestamp(LocalDateTime.now().minusDays(7))
                                    .build()
                    ))
                    .createdAt(LocalDateTime.now().minusDays(7))
                    .updatedAt(LocalDateTime.now().minusDays(5))
                    .build();

            issueRepository.save(issue3);

            Issue issue4 = Issue.builder()
                    .title("Implement pagination for issue list")
                    .description("Add server-side pagination with configurable page size")
                    .status(IssueStatus.OPEN)
                    .priority(IssuePriority.MEDIUM)
                    .projectId(project1.getId())
                    .assigneeId(null)
                    .tags(Arrays.asList("backend", "api", "pagination"))
                    .comments(new ArrayList<>())
                    .activityLogs(Arrays.asList(
                            ActivityLog.builder()
                                    .userId(projectOwner.getId())
                                    .userName(projectOwner.getName())
                                    .action("CREATED")
                                    .details("Issue created")
                                    .timestamp(LocalDateTime.now().minusDays(1))
                                    .build()
                    ))
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build();

            issueRepository.save(issue4);

            Issue issue5 = Issue.builder()
                    .title("Add input validation and error handling")
                    .description("Implement comprehensive validation for all API endpoints")
                    .status(IssueStatus.OPEN)
                    .priority(IssuePriority.LOW)
                    .projectId(project1.getId())
                    .assigneeId(null)
                    .tags(Arrays.asList("backend", "validation", "error-handling"))
                    .comments(new ArrayList<>())
                    .activityLogs(Arrays.asList(
                            ActivityLog.builder()
                                    .userId(projectOwner.getId())
                                    .userName(projectOwner.getName())
                                    .action("CREATED")
                                    .details("Issue created")
                                    .timestamp(LocalDateTime.now())
                                    .build()
                    ))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            issueRepository.save(issue5);

            // Create issues for project 2
            Issue issue6 = Issue.builder()
                    .title("Setup payment gateway integration")
                    .description("Integrate Stripe payment processing")
                    .status(IssueStatus.OPEN)
                    .priority(IssuePriority.CRITICAL)
                    .projectId(project2.getId())
                    .assigneeId(developer2.getId())
                    .tags(Arrays.asList("backend", "payment", "integration"))
                    .comments(new ArrayList<>())
                    .activityLogs(Arrays.asList(
                            ActivityLog.builder()
                                    .userId(projectOwner.getId())
                                    .userName(projectOwner.getName())
                                    .action("CREATED")
                                    .details("Issue created")
                                    .timestamp(LocalDateTime.now())
                                    .build()
                    ))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            issueRepository.save(issue6);

            log.info("Created {} issues", issueRepository.count());
            log.info("Database seeding completed successfully!");
            log.info("Test credentials:");
            log.info("  Admin: admin@issuetracker.com / admin123");
            log.info("  Project Owner: john@example.com / password123");
            log.info("  Developer: jane@example.com / password123");
            log.info("  Developer: bob@example.com / password123");
        };
    }
}
