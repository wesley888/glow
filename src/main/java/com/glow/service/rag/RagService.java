package com.glow.service.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Module 5 — RAG: Retrieval-Augmented Generation.
 * <p>
 * Three approaches demonstrated:
 * <ol>
 *   <li><b>QuestionAnswerAdvisor</b> — let Spring AI handle retrieval automatically</li>
 *   <li><b>Manual RAG</b> — retrieve docs yourself and build the context string</li>
 *   <li><b>Streaming RAG</b> — stream the answer token-by-token</li>
 * </ol>
 */
@Slf4j
@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final int topK;
    private final double similarityThreshold;

    public RagService(ChatClient chatClient,
                      VectorStore vectorStore,
                      @Value("${glow.rag.top-k:5}") int topK,
                      @Value("${glow.rag.similarity-threshold:0.7}") double similarityThreshold) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * 1. Automatic RAG via QuestionAnswerAdvisor.
     * The advisor retrieves relevant documents and injects them into the prompt
     * as context before the user question — fully transparent to the caller.
     */
    public String askWithAdvisor(String question) {
        return chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .query(question)
                                .topK(topK)
                                .similarityThreshold(similarityThreshold)
                                .build())
                        .build())
                .user(question)
                .call()
                .content();
    }

    /**
     * 2. Manual RAG — full control over the retrieval and prompt assembly.
     * Useful when you need to pre-process or filter documents before injection.
     */
    public String askManual(String question) {
        // Step 1: Retrieve relevant documents
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .build()
        );
        log.info("[RAG-MANUAL] Retrieved {} documents for query: {}", docs.size(), question);

        // Step 2: Assemble context string from retrieved chunks
        String context = docs.stream()
                .map(Document::getText)
                .reduce("", (a, b) -> a + "\n---\n" + b);

        // Step 3: Build the augmented prompt
        String augmentedPrompt = """
                Use the following context to answer the question.
                If the answer is not in the context, say "I don't have enough information."

                Context:
                %s

                Question: %s
                """.formatted(context, question);

        return chatClient.prompt()
                .user(augmentedPrompt)
                .call()
                .content();
    }

    /**
     * 3. Streaming RAG — stream the answer while the advisor retrieves context.
     */
    public Flux<String> askStream(String question) {
        return chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .query(question)
                                .topK(topK)
                                .build())
                        .build())
                .user(question)
                .stream()
                .content();
    }

    /**
     * Raw similarity search — returns matching document chunks directly.
     */
    public List<Document> search(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .build()
        );
    }
}
