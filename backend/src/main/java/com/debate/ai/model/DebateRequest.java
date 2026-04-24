package com.debate.ai.model;

public class DebateRequest {
    private String topic;
    private String proApiKey;
    private String conApiKey;
    private String proSystemPrompt;
    private String conSystemPrompt;
    private int rounds;
    private String proModel;
    private String conModel;

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getProModel() { return proModel; }
    public void setProModel(String proModel) { this.proModel = proModel; }

    public String getConModel() { return conModel; }
    public void setConModel(String conModel) { this.conModel = conModel; }

    public String getProApiKey() { return proApiKey; }
    public void setProApiKey(String proApiKey) { this.proApiKey = proApiKey; }

    public String getConApiKey() { return conApiKey; }
    public void setConApiKey(String conApiKey) { this.conApiKey = conApiKey; }

    public String getProSystemPrompt() { return proSystemPrompt; }
    public void setProSystemPrompt(String proSystemPrompt) { this.proSystemPrompt = proSystemPrompt; }

    public String getConSystemPrompt() { return conSystemPrompt; }
    public void setConSystemPrompt(String conSystemPrompt) { this.conSystemPrompt = conSystemPrompt; }

    public int getRounds() { return rounds; }
    public void setRounds(int rounds) { this.rounds = rounds; }
}
