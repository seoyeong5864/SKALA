package com.skala.ai.lab.web;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.skala.ai.lab.dto.AnswerDto;
import com.skala.ai.lab.dto.Chunk;
import com.skala.ai.lab.dto.RagQueryRequest;
import com.skala.ai.lab.service.IngestService;
import com.skala.ai.lab.service.IngestService.IngestResult;
import com.skala.ai.lab.service.RagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lab2")
@Tag(name = "DAY2 실습 / RAG")
public class RagController {
    private final IngestService ingestService;
    private final RagService ragService;
    private final VectorStore vectorStore;

    public RagController(IngestService ingestService, RagService ragService, VectorStore vectorStore) {
        this.ingestService = ingestService;
        this.ragService = ragService;
        this.vectorStore = vectorStore;
    }

    @PostMapping("/ingest")
    @Operation(summary = "docs 전체 인제스트",
            description = "resources/docs의 모든 문서를 읽어 벡터 스토어에 (재)적재한다. 임베딩 API를 호출하므로 비용이 발생한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인제스트 성공, 파일별 처리 결과 반환")})
    public List<IngestResult> ingest() {
        return ingestService.ingestAll();
    }

    @PostMapping(value = "/ingest/upload", consumes = "multipart/form-data")
    @Operation(summary = "문서 업로드 인제스트",
            description = "임의의 문서 파일을 업로드해 벡터 스토어에 적재한다. 본문에 문서 유형/담당 부서 헤더가 없으므로 docType/dept를 직접 지정한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인제스트 성공")})
    public IngestResult ingestUpload(
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "문서 유형") @RequestParam(defaultValue = "미분류") String docType,
            @Parameter(description = "담당 부서") @RequestParam(defaultValue = "미분류") String dept) throws IOException {
        Resource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        return ingestService.ingest(resource, docType, dept);
    }

    // 답변을 만들기 전에 검색 결과와 점수부터 눈으로 확인한다
    @GetMapping("/retrieve")
    @Operation(summary = "유사도 검색 결과 확인",
            description = "질문으로 벡터 스토어를 검색해 출처·유사도 점수·본문 일부를 그대로 반환한다. 점수가 낮으면 근거가 없다는 뜻이다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공, topK개 청크 반환")})
    public List<Chunk> retrieve(
            @Parameter(description = "질문", example = "반품 기한") @RequestParam String q,
            @Parameter(description = "가져올 청크 개수") @RequestParam(defaultValue = "4") int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                    .query(q)
                    .topK(topK)
                    .similarityThresholdAll() // TODO: 실제 점수 분포 확인 후 적정 threshold로 되돌리기
                    .build())
                .stream()
                .map(d -> new Chunk(
                        String.valueOf(d.getMetadata().get("source")),
                        d.getScore(),
                        snippet(d.getText(), 120)))
                .toList();
    }

    private String snippet(String text, int maxLen) {
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    @PostMapping("/ask")
    @Operation(summary = "근거 기반 질의응답",
            description = "검색된 근거만 사용해 답변한다. 근거가 없으면 모델을 호출하지 않고 거절 응답을 반환한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "답변 성공 (근거 부족 시 grounded=false로 거절 응답)")})
    public AnswerDto ask(@RequestBody RagQueryRequest request) {
        return ragService.ask(request.question(), request.dept(), request.sessionId());
    }
}
