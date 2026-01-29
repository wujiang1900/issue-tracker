package com.issuetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
