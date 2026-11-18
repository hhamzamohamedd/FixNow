package com.fix.fixnow.service;

import com.fix.fixnow.service.GeminiAssistantService.AiAssistantUnavailableException;
import com.fix.fixnow.service.GeminiAssistantService.AiAssistantRateLimitException;
import com.fix.fixnow.service.GeminiAssistantService.ChatTurn;
import com.fix.fixnow.service.GeminiAssistantService.RequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;
import static org.hamcrest.Matchers.containsString;

class GeminiAssistantServiceTest {

    @Test
    void sendsConversationAndCustomerRequestStatusToGemini() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiAssistantService service = new GeminiAssistantService(builder, "test-key", "gemini-2.5-flash");

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=test-key"))
                .andExpect(method(POST))
                .andExpect(content().string(containsString("\"role\":\"model\"")))
                .andExpect(content().string(containsString("Request #42: ACCEPTED")))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Your request is accepted.\"}]}}]}",
                        MediaType.APPLICATION_JSON
                ));

        String reply = service.reply(
                "What is my request status?",
                List.of(new ChatTurn("user", "Can you help me?"), new ChatTurn("model", "Of course.")),
                List.of(new RequestStatus(42L, "ACCEPTED"))
        );

        assertEquals("Your request is accepted.", reply);
        server.verify();
    }

    @Test
    void reportsMissingApiKeyWithoutCallingGemini() {
        GeminiAssistantService service = new GeminiAssistantService(RestClient.builder(), "", "gemini-2.5-flash");

        AiAssistantUnavailableException exception = assertThrows(
                AiAssistantUnavailableException.class,
                () -> service.reply("Hello", List.of(), List.of())
        );

        assertEquals("The AI assistant is not configured. Set GEMINI_API_KEY on the server.", exception.getMessage());
    }

    @Test
    void limitsChatRequestsPerCustomer() {
        GeminiAssistantService service = new GeminiAssistantService(RestClient.builder(), "test-key", "gemini-3.8-flash");

        for (int request = 0; request < 10; request++) {
            service.checkRateLimit(42L);
        }

        assertThrows(AiAssistantRateLimitException.class, () -> service.checkRateLimit(42L));
        service.checkRateLimit(43L);
    }
}
