package com.skala.ai.lab.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skala.ai.lab.dto.ChatRequest;
import com.skala.ai.lab.dto.ChatResponse;
import com.skala.ai.lab.service.AgentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lab3")
@Tag(name = "DAY3 실습 / 상담 에이전트")
public class AgentController {

    private final AgentService service;

    public AgentController(AgentService service) {
        this.service = service;
    }

    @PostMapping("/chat")
    @Operation(summary = "상담 에이전트",
            description = "규정 질문은 근거 검색(RAG) 후 출처와 함께 답하고, 주문/환불 질문은 도구를 호출해 답한다. "
                    + "인젝션 문장은 차단되며, 같은 sessionId로 여러 번 호출하면 대화가 이어진다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "응답 성공")})
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return service.chat(request.question(), request.userId(), request.sessionId());
    }

    @GetMapping("/chat/history")
    @Operation(summary = "대화 이력 조회 (Advisor 순서 검증용)",
            description = "차단된 문장이 메모리에 남았는지 확인할 때 쓴다. SafetyAdvisor가 MessageChatMemoryAdvisor보다 "
                    + "먼저 실행되면(정상 순서) 차단된 문장은 여기 나타나지 않는다.")
    public List<String> history(@Parameter(description = "세션 ID") @RequestParam String sessionId) {
        return service.history(sessionId);
    }
}
