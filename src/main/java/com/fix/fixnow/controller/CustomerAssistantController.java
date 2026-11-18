package com.fix.fixnow.controller;

import com.fix.fixnow.model.ServiceRequest;
import com.fix.fixnow.security.SessionAuthConstants;
import com.fix.fixnow.service.CustomerService;
import com.fix.fixnow.service.GeminiAssistantService;
import com.fix.fixnow.service.GeminiAssistantService.AiAssistantRateLimitException;
import com.fix.fixnow.service.GeminiAssistantService.AiAssistantUnavailableException;
import com.fix.fixnow.service.GeminiAssistantService.ChatTurn;
import com.fix.fixnow.service.GeminiAssistantService.RequestStatus;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/customer/assistant")
public class CustomerAssistantController {

    private static final String CONVERSATION_SESSION_KEY = "fixnow.customer.assistant.conversation";
    private static final int MAX_HISTORY_MESSAGES = 12;

    private final CustomerService customerService;
    private final GeminiAssistantService assistantService;

    public CustomerAssistantController(CustomerService customerService, GeminiAssistantService assistantService) {
        this.customerService = customerService;
        this.assistantService = assistantService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request, HttpSession session) {
        Long userId = currentUserId(session);
        assistantService.checkRateLimit(userId);
        String message = request.message().trim();

        Object storedConversation = session.getAttribute(CONVERSATION_SESSION_KEY);
        Conversation conversation = storedConversation instanceof Conversation saved
                && saved.userId().equals(userId)
                ? saved
                : new Conversation(userId, List.of());

        List<RequestStatus> requestStatuses = customerService.getMyRequests(userId).stream()
                .filter(serviceRequest -> serviceRequest.getId() != null)
                .sorted(Comparator.comparing(ServiceRequest::getId).reversed())
                .limit(20)
                .map(this::toRequestStatus)
                .toList();

        String reply = assistantService.reply(message, conversation.turns(), requestStatuses);

        List<ChatTurn> updatedTurns = new ArrayList<>(conversation.turns());
        updatedTurns.add(new ChatTurn("user", message));
        updatedTurns.add(new ChatTurn("model", reply));
        if (updatedTurns.size() > MAX_HISTORY_MESSAGES) {
            updatedTurns = new ArrayList<>(updatedTurns.subList(
                    updatedTurns.size() - MAX_HISTORY_MESSAGES,
                    updatedTurns.size()
            ));
        }
        session.setAttribute(CONVERSATION_SESSION_KEY, new Conversation(userId, List.copyOf(updatedTurns)));

        return new ChatResponse(reply);
    }

    @ExceptionHandler(AiAssistantUnavailableException.class)
    public ResponseEntity<ChatError> handleAssistantUnavailable(AiAssistantUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ChatError(ex.getMessage()));
    }

    @ExceptionHandler(AiAssistantRateLimitException.class)
    public ResponseEntity<ChatError> handleRateLimit(AiAssistantRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ChatError(ex.getMessage()));
    }

    private RequestStatus toRequestStatus(ServiceRequest request) {
        return new RequestStatus(request.getId(), request.getStatus());
    }

    private Long currentUserId(HttpSession session) {
        Object userId = session.getAttribute(SessionAuthConstants.AUTH_USER_ID);
        if (userId instanceof Long id) {
            return id;
        }
        if (userId instanceof Number number) {
            return number.longValue();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    public record ChatRequest(@NotBlank @Size(max = 2000) String message) {
    }

    public record ChatResponse(String reply) {
    }

    public record ChatError(String error) {
    }

    private record Conversation(Long userId, List<ChatTurn> turns) {
    }
}
