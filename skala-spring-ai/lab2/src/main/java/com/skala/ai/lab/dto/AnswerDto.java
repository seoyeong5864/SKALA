package com.skala.ai.lab.dto;

import java.util.List;

public record AnswerDto(String answer, List<String> sources, boolean grounded) {
    public static AnswerDto unknown() {
        return new AnswerDto("확인할 수 없습니다.", List.of(), false);
    }
}
