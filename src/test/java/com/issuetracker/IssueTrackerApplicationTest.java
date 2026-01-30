package com.issuetracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for the IssueTrackerApplication.
 * This class ensures that the application context loads successfully.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class IssueTrackerApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Test to verify that the application context loads successfully.
     */
    @Test
    void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should not be null");
    }
}
