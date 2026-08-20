package com.skala.ai.lab.advisor;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;

/**
 * 프롬프트 인젝션 차단.
 *
 * <p>{@code BaseAdvisor}가 아니라 {@code CallAdvisor}를 직접 구현하는 이유 — 차단 시 체인을
 * 아예 진행시키지 않아야 한다. {@code BaseAdvisor}의 기본 템플릿은 before() 뒤에 항상
 * chain.nextCall()을 호출하므로 "저장 전에 끊기"가 불가능하다.
 *
 * <p>order는 MessageChatMemoryAdvisor(order {@code HIGHEST_PRECEDENCE+200})보다 작게(더 바깥) 둔다 —
 * 순서가 바뀌면 차단된 문장이 이미 메모리에 저장된 뒤라 다음 턴부터 계속 새어 나온다.
 */
public class SafetyAdvisor implements CallAdvisor {

    private static final Logger audit = LoggerFactory.getLogger("AI_AUDIT");

    private static final Pattern INJECTION = Pattern.compile(
            "이전\\s*지시.*(무시|잊)|시스템\\s*프롬프트|프롬프트를?\\s*(보여|알려)달라?|"
          + "너는\\s*이제|역할\\s*(을)?\\s*무시|지금부터\\s*너는|"
          + "ignore\\s+(all\\s+)?(previous|prior)\\s+instructions?|system\\s*prompt",
            Pattern.CASE_INSENSITIVE);

    @Override
    public String getName() {
        return "SafetyAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 60;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String text = request.prompt().getUserMessage() == null
                ? "" : request.prompt().getUserMessage().getText();

        if (text != null && INJECTION.matcher(text).find()) {
            audit.warn("BLOCKED_INJECTION text={}", text);
            return refuse(request);
        }

        return chain.nextCall(request); // 통과 — 이후 메모리 저장·근거 검색으로 이어진다
    }

    private ChatClientResponse refuse(ChatClientRequest request) {
        ChatResponse chatResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("요청을 처리할 수 없습니다. 다른 방식으로 다시 말씀해 주세요."))));
        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .context(request.context())
                .build();
    }
}
