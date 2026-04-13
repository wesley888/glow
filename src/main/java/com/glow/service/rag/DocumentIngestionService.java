package com.glow.service.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Module 5 — RAG: Document Ingestion Pipeline.
 * <p>
 * Pipeline: Upload → Read → Split → Embed → Store
 * <p>
 * Supports any format Tika can parse: PDF, Word, Excel, HTML, Markdown, plain text, etc.
 */
@Slf4j
@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    /**
     * Default text splitter:
     * - 800 tokens per chunk
     * - 150 token overlap (to avoid losing context across chunk boundaries)
     */
    private final TokenTextSplitter textSplitter = new TokenTextSplitter(800, 150, 5, 10000, true);

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Ingest a file uploaded via HTTP multipart form.
     *
     * @return number of chunks stored in the vector store
     */
    public int ingestFile(MultipartFile file) throws IOException {
        Resource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        return ingestResource(resource, Map.of(
                "source", file.getOriginalFilename(),
                "contentType", file.getContentType()
        ));
    }

    /**
     * Ingest a classpath/file-system resource with custom metadata.
     *
     * @return number of chunks stored
     */
    public int ingestResource(Resource resource, Map<String, Object> metadata) {
        log.info("[RAG] Ingesting resource: {}", resource.getFilename());

        // Step 1: Read — Tika handles PDF, Word, HTML, etc. automatically
        List<Document> rawDocs = new TikaDocumentReader(resource).get();
        log.info("[RAG] Read {} document(s)", rawDocs.size());

        // Step 2: Attach metadata to each document
        List<Document> docsWithMeta = rawDocs.stream()
                .map(doc -> {
                    Map<String, Object> merged = new java.util.HashMap<>(doc.getMetadata());
                    merged.putAll(metadata);
                    return new Document(doc.getText(), merged);
                })
                .toList();

        // Step 3: Split into chunks
        List<Document> chunks = textSplitter.apply(docsWithMeta);
        log.info("[RAG] Split into {} chunks", chunks.size());

        // Step 4: Embed + Store (EmbeddingModel is called internally by VectorStore)
        vectorStore.add(chunks);
        log.info("[RAG] Stored {} chunks in vector store", chunks.size());

        return chunks.size();
    }

    /**
     * Ingest plain text directly (no file needed).
     */
    public int ingestText(String text, String source) {
        Document doc = new Document(text, Map.of("source", source));
        List<Document> chunks = textSplitter.apply(List.of(doc));
        vectorStore.add(chunks);
        log.info("[RAG] Ingested text '{}' → {} chunks", source, chunks.size());
        return chunks.size();
    }
}
