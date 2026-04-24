package com.debate.ai.controller;

import com.debate.ai.model.ChatRequest;
import com.debate.ai.model.DebateRequest;
import com.debate.ai.service.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DebateWebSocketHandler extends TextWebSocketHandler {

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public DebateWebSocketHandler(LlmClient llmClient) {
        this.llmClient = llmClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if ("PING".equals(payload)) {
            session.sendMessage(new TextMessage("PONG"));
            return;
        }

        DebateRequest config = objectMapper.readValue(payload, DebateRequest.class);
        
        executorService.submit(() -> runDebate(session, config));
    }

    private void runDebate(WebSocketSession session, DebateRequest config) {
        try {
            // Keep a running conversational history
            String conversationContext = "本次辩论的主题是：" + config.getTopic() + "\n";
            
            // Assume compatible OpenAI endpoints (you could also make these configurable)
            String qwenUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            String deepseekUrl = "https://api.deepseek.com/chat/completions";

            sendWsMessage(session, "INFO", "SYSTEM", "辩论已启动，准备接入模型...");

            for (int round = 1; round <= config.getRounds(); round++) {
                if (!session.isOpen()) break;

                // --- PRO TURN ---
                sendWsMessage(session, "START_TURN", "PRO", "第 " + round + " 回合 - 正方发言");
                ChatRequest proReq = buildRequest(config.getProModel() != null ? config.getProModel() : "deepseek-chat", config.getProSystemPrompt(), conversationContext);
                
                String proFullReply = llmClient.streamChatCompletion(deepseekUrl, config.getProApiKey(), proReq, token -> {
                    sendWsMessage(session, "TOKEN", "PRO", token);
                });
                sendWsMessage(session, "END_TURN", "PRO", "");
                
                // Append what was just said
                conversationContext += "\n[正方]: " + proFullReply + "\n";
                
                if (!session.isOpen()) break;

                // --- CON TURN ---
                sendWsMessage(session, "START_TURN", "CON", "第 " + round + " 回合 - 反方反驳");
                ChatRequest conReq = buildRequest(config.getConModel() != null ? config.getConModel() : "qwen-turbo", config.getConSystemPrompt(), conversationContext + "\n请根据以上所有的陈述作为反方展开你的下一轮反驳。");
                
                String conFullReply = llmClient.streamChatCompletion(qwenUrl, config.getConApiKey(), conReq, token -> {
                    sendWsMessage(session, "TOKEN", "CON", token);
                });
                sendWsMessage(session, "END_TURN", "CON", "");

                // Append
                conversationContext += "\n[反方]: " + conFullReply + "\n";
            }

            sendWsMessage(session, "FINISH", "SYSTEM", "所有回合已结束！");

        } catch (Exception e) {
            e.printStackTrace();
            sendWsMessage(session, "ERROR", "SYSTEM", "辩论过程中发生异常: " + e.getMessage());
        }
    }

    private ChatRequest buildRequest(String model, String sysPrompt, String userContext) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        if (sysPrompt != null && !sysPrompt.isEmpty()) {
            messages.add(new ChatRequest.Message("system", sysPrompt));
        }
        messages.add(new ChatRequest.Message("user", userContext));
        return new ChatRequest(model, messages);
    }

    private void sendWsMessage(WebSocketSession session, String type, String role, String content) {
        if (!session.isOpen()) return;
        Map<String, String> msg = new HashMap<>();
        msg.put("type", type);
        msg.put("role", role);
        msg.put("content", content);
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
