package com.skala.helpdesk.rag;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class IngestService {

    private final VectorStore vectorStore;

    public IngestService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
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
