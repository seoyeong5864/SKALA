package com.skala.helpdesk.rag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@Service
public class IngestService {

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourceResolver;

    public IngestService(VectorStore vectorStore, ResourceLoader resourceLoader) {
        this.vectorStore = vectorStore;
        this.resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader);
    }

    // 기동 직후 docs/ 전체를 한 번에 다시 채운다
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
                results.add(ingest(file, "정책문서", "CS팀"));
            } catch (Exception e) {
                log.warn("인제스트 실패 file={}", file.getFilename(), e);
            }
        }
        log.info("전체 인제스트 완료 files={}", results.size());
        return results;
    }

    public IngestResult ingest(Resource file, String docType, String dept) {
        String source = file.getFilename();
        vectorStore.delete("source == '" + source + "'");

        List<Document> raw = new TikaDocumentReader(file).get();
        List<Document> chunks = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .build()
                .apply(raw);

        List<Document> enriched = chunks.stream().map(c -> {
            Map<String, Object> m = new HashMap<>(c.getMetadata());
            m.put("source", source);
            m.put("dept", dept);
            m.put("docType", docType);
            m.put("version", LocalDate.now().toString());
            return new Document(c.getText(), m);
        }).toList();

        vectorStore.add(enriched);
        return new IngestResult(source, enriched.size());
    }
}
