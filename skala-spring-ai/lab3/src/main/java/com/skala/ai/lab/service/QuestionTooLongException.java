package com.skala.ai.lab.service;

public class QuestionTooLongException extends RuntimeException {
    public QuestionTooLongException(int length, int max) {
        super("질문이 너무 깁니다(%d자, 최대 %d자). 짧게 나눠서 질문해 주세요.".formatted(length, max));
    }
}
