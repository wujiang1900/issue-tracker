package com.issuetracker.websocket;

import com.issuetracker.dto.response.IssueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IssueEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishIssueCreated(IssueResponse issue) {
        log.info("Publishing issue created event for issue: {}", issue.getId());
        messagingTemplate.convertAndSend("/topic/issues/" + issue.getProjectId(),
                Map.of("type", "CREATED", "issue", issue));
    }

    public void publishIssueUpdated(IssueResponse issue) {
        log.info("Publishing issue updated event for issue: {}", issue.getId());
        messagingTemplate.convertAndSend("/topic/issues/" + issue.getProjectId(),
                Map.of("type", "UPDATED", "issue", issue));
        messagingTemplate.convertAndSend("/topic/issue/" + issue.getId(),
                Map.of("type", "UPDATED", "issue", issue));
    }

    public void publishIssueDeleted(String issueId, String projectId) {
        log.info("Publishing issue deleted event for issue: {}", issueId);
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "DELETED");
        payload.put("issueId", issueId);
        messagingTemplate.convertAndSend("/topic/issues/" + projectId, payload);
    }
}
