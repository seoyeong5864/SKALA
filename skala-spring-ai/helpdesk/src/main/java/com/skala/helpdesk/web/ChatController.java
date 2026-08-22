package com.skala.helpdesk.web;

import java.security.Principal;
import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skala.helpdesk.chat.AnswerDto;
import com.skala.helpdesk.chat.AskRequest;
import com.skala.helpdesk.chat.HelpDeskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "HelpDesk 상담")
public class ChatController {

    private final HelpDeskService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatController(HelpDeskService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "질문에 답한다 (동기)",
            description = "사내 규정 질문은 근거 검색(RAG) 후 출처와 함께 답하고, 주문·교환·환불 질문은 도구를 호출해 답한다. "
                    + "같은 sessionId로 다시 호출하면 이전 대화 맥락이 이어진다. 로그인 필요.")
    AnswerDto ask(@RequestBody AskRequest req,
                  @Parameter(hidden = true) Principal user) {
        return service.ask(req.question(), user.getName(), req.sessionId());
    }

    @PostMapping(value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "질문에 답한다 (SSE 스트리밍)",
            description = "동기 API와 동일하게 동작하되, 답변 토큰을 event: token으로 순차 전송하고 "
                    + "마지막에 event: sources로 출처 목록(JSON)을 한 번 더 보낸다. 로그인 필요.")
    Flux<ServerSentEvent<String>> stream(@RequestBody AskRequest req,
                                          @Parameter(hidden = true) Principal user) {
        return service.stream(req.question(), user.getName(), req.sessionId())
                .map(c -> ServerSentEvent.builder(c).event("token").build())
                .concatWith(Mono.fromCallable(() ->
                        ServerSentEvent.builder(toJson(service.lastSources(user.getName(), req.sessionId())))
                                .event("sources").build()))
                .timeout(Duration.ofSeconds(60));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("응답 직렬화 실패", e);
        }
    }
}
