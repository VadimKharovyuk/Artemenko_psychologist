package com.example.artemenko_psychologist.service.impl;

import java.util.List;
import java.util.Map;


public interface EmailSender {
    void sendHtmlMessage(String to, String subject, String template, Map<String, Object> context);
    void sendBulkHtmlMessage(List<String> emails, String subject, String template, Map<String, Object> context);
}