package com.skala.helpdesk.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.skala.helpdesk.rag.IngestResult;
import com.skala.helpdesk.rag.IngestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "HelpDesk 관리 / 문서·검색")
public class AdminController {

    private final VectorStore vectorStore;
    private final IngestService ingestService;

    public AdminController(VectorStore vectorStore, IngestService ingestService) {
        this.vectorStore = vectorStore;
        this.ingestService = ingestService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ingest/all")
    @Operation(summary = "docs/ 전체 인제스트",
            description = "resources/docs의 정책 문서 전체를 다시 읽어 벡터 스토어에 (재)적재한다. "
                    + "인메모리 VectorStore가 비어 있을 때(재시작 직후 등) 한 번에 채우는 용도. ADMIN 인증 필요.")
    public List<IngestResult> ingestAll() {
        return ingestService.ingestAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/ingest", consumes = "multipart/form-data")
    @Operation(summary = "문서 업로드 인제스트",
            description = "사내 문서를 업로드해 청크로 나누고 벡터 스토어에 (재)적재한다. ADMIN 인증 필요.")
    public IngestResult ingest(
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/chunks")
    @Operation(summary = "적재된 청크 검색 확인",
            description = "질문으로 벡터 스토어를 검색해 출처·버전·유사도 점수·본문 일부를 그대로 반환한다. "
                    + "인제스트 직후 실제로 무엇이 들어갔는지 확인할 때 쓴다. ADMIN 인증 필요.")
    public List<Map<String, Object>> inspect(
            @Parameter(description = "검색 질의") @RequestParam String q,
            @Parameter(description = "가져올 청크 개수") @RequestParam(defaultValue = "5") int topK) {
        var hits = vectorStore.similaritySearch(
                SearchRequest.builder().query(q).topK(topK).build());

        return hits.stream().map(d -> Map.<String, Object>of(
                "source", d.getMetadata().get("source"),
                "version", d.getMetadata().get("version"),
                "score", d.getScore(),
                "preview", d.getText().substring(0, Math.min(160, d.getText().length()))
        )).toList();
    }
}
