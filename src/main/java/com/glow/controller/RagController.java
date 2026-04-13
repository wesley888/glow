package com.glow.controller;

import com.glow.service.rag.DocumentIngestionService;
import com.glow.service.rag.RagService;
import org.springframework.ai.document.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

/**
 * Module 5 — RAG API.
 * <pre>
 * POST /api/rag/ingest/file    — upload a file (PDF, Word, etc.) into vector store
 * POST /api/rag/ingest/text    — ingest plain text directly
 * POST /api/rag/ask            — ask a question (auto RAG via advisor)
 * POST /api/rag/ask/manual     — ask a question (manual retrieval)
 * POST /api/rag/ask/stream     — streaming RAG answer (SSE)
 * GET  /api/rag/search         — raw similarity search (returns document chunks)
 * </pre>
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final DocumentIngestionService ingestionService;
    private final RagService ragService;

    public RagController(DocumentIngestionService ingestionService, RagService ragService) {
        this.ingestionService = ingestionService;
        this.ragService = ragService;
    }

    @PostMapping("/ingest/file")
    public String ingestFile(@RequestParam("file") MultipartFile file) throws IOException {
        int chunks = ingestionService.ingestFile(file);
        return "Ingested '%s' → %d chunks stored.".formatted(file.getOriginalFilename(), chunks);
    }

    @PostMapping("/ingest/text")
    public String ingestText(@RequestParam String text,
                             @RequestParam(defaultValue = "manual-input") String source) {
        int chunks = ingestionService.ingestText(text, source);
        return "Ingested text '%s' → %d chunks stored.".formatted(source, chunks);
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String question) {
        return ragService.askWithAdvisor(question);
    }

    @PostMapping("/ask/manual")
    public String askManual(@RequestParam String question) {
        return ragService.askManual(question);
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@RequestParam String question) {
        return ragService.askStream(question);
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query) {
        return ragService.search(query);
    }
}
