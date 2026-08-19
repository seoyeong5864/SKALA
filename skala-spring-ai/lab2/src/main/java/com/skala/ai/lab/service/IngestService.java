package com.skala.ai.lab.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

// 읽기 -> 나누기 -> 메타데이터 보강 -> 저장
@Service
public class IngestService {

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    private static final Pattern DOC_TYPE_PATTERN = Pattern.compile("문서\\s*유형\\s*[:：]\\s*(.+)");
    private static final Pattern DEPT_PATTERN = Pattern.compile("담당\\s*부서\\s*[:：]\\s*(\\S+)");
    private static final Pattern VERSION_PATTERN = Pattern.compile("버전\\s*[:：]\\s*(\\S+)");

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourceResolver;

    public IngestService(VectorStore vectorStore, ResourceLoader resourceLoader) {
        this.vectorStore = vectorStore;
        this.resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader);
    }

    public record IngestResult(String source, int chunks) {}

    // ⑤ Orchestrate — docs/ 폴더 전체를 찾아 파일 단위로 ingest() 반복 호출
    public List<IngestResult> ingestAll() {
        Resource[] files;
        try {
            files = resourceResolver.getResources("classpath:docs/*.md");
        } catch (IOException e) {
            throw new UncheckedIOException("docs 디렉터리를 읽을 수 없습니다", e);
        }

        List<IngestResult> results = new ArrayList<>();
        for (Resource file : files) {
            try {
                results.add(ingest(file));
            } catch (Exception e) {
                log.warn("인제스트 실패 file={}", file.getFilename(), e);
            }
        }

        int totalChunks = results.stream().mapToInt(IngestResult::chunks).sum();
        log.info("전체 인제스트 완료 files={} totalChunks={}", results.size(), totalChunks);
        return results;
    }

    // ① Read — Tika 하나로 PDF·DOCX·HTML·TXT를 모두 읽는다
    // docs/ 폴더의 정책 문서처럼 본문에 "문서 유형"/"담당 부서" 헤더가 있는 경우 자동 파싱한다
    public IngestResult ingest(Resource file) {
        return ingest(file, null, null);
    }

    // 업로드 문서처럼 본문에 헤더가 없는 경우, docType/dept를 호출자가 직접 지정한다
    public IngestResult ingest(Resource file, String docTypeOverride, String deptOverride) {
        List<Document> raw = new TikaDocumentReader(file).get();
        String header = raw.isEmpty() ? "" : raw.get(0).getText();

        String source = file.getFilename() == null ? "unknown" : file.getFilename();
        String docType = docTypeOverride != null ? docTypeOverride : extract(header, DOC_TYPE_PATTERN, "미분류");
        String dept = deptOverride != null ? deptOverride : extract(header, DEPT_PATTERN, "미분류");
        String version = extract(header, VERSION_PATTERN, "미상");

        deleteExisting(source);

        // ② Split — 정책 문서 섹션 하나(~150~220자) 단위로 쪼개지도록 기본값보다 작게 설정
        List<Document> chunks = TokenTextSplitter.builder()
                .withChunkSize(300)
                .withMinChunkSizeChars(100)
                .withKeepSeparator(true)
                .build()
                .apply(raw);

        // ③ Enrich — 인제스트 때 안 넣은 메타데이터는 나중에 넣을 수 없다
        List<Document> enriched = chunks.stream()
                .map(chunk -> {
                    Map<String, Object> meta = new HashMap<>(chunk.getMetadata());
                    meta.put("source", source);
                    meta.put("docType", docType);
                    meta.put("dept", dept);
                    meta.put("version", version);
                    return new Document(chunk.getText(), meta);
                })
                .toList();

        // ④ Write — 임베딩 + 저장
        vectorStore.add(enriched);
        log.info("인제스트 완료 source={} docType={} dept={} version={} chunks={}",
                source, docType, dept, version, enriched.size());

        return new IngestResult(source, enriched.size());
    }

    private String extract(String text, Pattern pattern, String fallback) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1).trim() : fallback;
    }

    /** 재색인 대비 — 이 문서에서 나온 청크를 모두 지운다. */
    private void deleteExisting(String source) {
        try {
            vectorStore.delete("source == '" + source + "'");
        } catch (Exception e) {
            // 인메모리 스토어 등 필터 삭제를 지원하지 않는 구현도 있다
            log.debug("기존 청크 삭제를 건너뛴다: {}", e.getMessage());
        }
    }
}
