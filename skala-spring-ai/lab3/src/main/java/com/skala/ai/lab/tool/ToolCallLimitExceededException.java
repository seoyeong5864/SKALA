package com.skala.ai.lab.tool;

/** 도구를 호출하는 시점에 던져 Spring AI가 이 메시지를 도구 결과로 모델에게 돌려주게 한다. */
public class ToolCallLimitExceededException extends RuntimeException {
    public ToolCallLimitExceededException() {
        super("이 대화에서 도구 호출 한도를 초과했습니다. 새 대화로 다시 시도해 주세요.");
    }
}
