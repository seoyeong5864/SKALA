package com.skala.ai.lab;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skala.ai.lab.dto.AnswerDto;
import com.skala.ai.lab.service.RagService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RagGoldenSetTest {

    private static final Logger log = LoggerFactory.getLogger(RagGoldenSetTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RagService service;

    record Golden(String q, List<String> must, String src) {}

    @Test
    @Tag("eval") // 모델을 부르므로 기본 테스트에서는 제외한다 (./gradlew test -Peval)
    void 골든_세트_평가() throws Exception {
        List<Golden> golden = mapper.readValue(
                new ClassPathResource("golden.json").getInputStream(),
                new TypeReference<List<Golden>>() {});

        int pass = 0;
        for (Golden g : golden) {
            AnswerDto a = service.ask(g.q());
            boolean hit = g.must().stream().allMatch(k -> a.answer().contains(k));
            boolean cite = g.src() == null
                    || a.sources().stream().anyMatch(s -> s.contains(g.src()));
            if (hit && cite) {
                pass++;
            } else {
                log.warn("실패: {}\n 답변: {}\n 출처: {}", g.q(), a.answer(), a.sources());
            }
        }
        log.info("통과 {}/{}", pass, golden.size());
        assertThat(pass).isGreaterThanOrEqualTo(8); // 기준선을 코드에 박아 둔다
    }

    // 실패를 두 종류로 나눠 적는다 — 고칠 곳이 완전히 다르다
    // ㉠ 근거를 못 찾았다 → 청킹 · 임베딩 · top-k · 질문 변환
    // ㉡ 찾았고 잘못 답했다 → 프롬프트 · 모델 · 근거 포맷
}
