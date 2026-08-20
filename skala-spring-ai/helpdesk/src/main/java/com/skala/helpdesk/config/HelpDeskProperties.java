package com.skala.helpdesk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helpdesk")
public record HelpDeskProperties(Rag rag, Memory memory) {

    public record Rag(int topK, double threshold) {}

    public record Memory(int max) {}
}
