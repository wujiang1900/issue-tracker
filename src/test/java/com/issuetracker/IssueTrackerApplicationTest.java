package com.issuetracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for the IssueTrackerApplication.
 * This class ensures the correct behavior of the main method in the application class.
 */
@SpringBootTest
public class IssueTrackerApplicationTest {

    /**
     * Test to verify that the application context loads and the main method runs without throwing exceptions.
     */
    @Test
    void testMainMethodRunsSuccessfully() {
        String[] args = {};
        assertDoesNotThrow(() -> IssueTrackerApplication.main(args));
    }
}
