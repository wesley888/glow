package com.glow.service.multimodal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.audio.speech.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.audio.transcription.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Module 9 — Multimodal.
 * <p>
 * Covers four capabilities:
 * <ol>
 *   <li>Vision — describe / analyze uploaded images</li>
 *   <li>Image generation — DALL-E 3 text-to-image</li>
 *   <li>Speech-to-text — Whisper audio transcription</li>
 *   <li>Text-to-speech — OpenAI TTS audio synthesis</li>
 * </ol>
 */
@Slf4j
@Service
public class MultimodalService {

    private final ChatClient chatClient;
    private final ImageModel imageModel;
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final OpenAiAudioSpeechModel speechModel;

    public MultimodalService(ChatClient chatClient,
                             ImageModel imageModel,
                             OpenAiAudioTranscriptionModel transcriptionModel,
                             OpenAiAudioSpeechModel speechModel) {
        this.chatClient = chatClient;
        this.imageModel = imageModel;
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    // ===== 1. Vision — Image Understanding =====

    /**
     * Describe an uploaded image using GPT-4o vision.
     */
    public String describeImage(MultipartFile imageFile) throws IOException {
        log.info("[VISION] Describing image: {}", imageFile.getOriginalFilename());
        Resource imageResource = new ByteArrayResource(imageFile.getBytes());
        String mimeType = imageFile.getContentType() != null
                ? imageFile.getContentType() : "image/jpeg";

        return chatClient.prompt()
                .user(u -> u
                        .text("Describe this image in detail. Include objects, colors, composition, and any text visible.")
                        .media(MimeTypeUtils.parseMimeType(mimeType), imageResource))
                .call()
                .content();
    }

    /**
     * Ask a specific question about an uploaded image.
     */
    public String analyzeImage(MultipartFile imageFile, String question) throws IOException {
        Resource imageResource = new ByteArrayResource(imageFile.getBytes());
        String mimeType = imageFile.getContentType() != null
                ? imageFile.getContentType() : "image/jpeg";

        return chatClient.prompt()
                .user(u -> u
                        .text(question)
                        .media(MimeTypeUtils.parseMimeType(mimeType), imageResource))
                .call()
                .content();
    }

    // ===== 2. Image Generation — DALL-E 3 =====

    /**
     * Generate an image from a text description. Returns the image URL.
     */
    public String generateImage(String prompt, String size, String quality) {
        log.info("[IMAGE-GEN] Prompt: {}", prompt);
        ImageResponse response = imageModel.call(
                new ImagePrompt(prompt,
                        OpenAiImageOptions.builder()
                                .model("dall-e-3")
                                .size(size)       // "1024x1024", "1792x1024", "1024x1792"
                                .quality(quality) // "standard" or "hd"
                                .N(1)
                                .build())
        );
        String url = response.getResult().getOutput().getUrl();
        log.info("[IMAGE-GEN] Generated URL: {}", url);
        return url;
    }

    // ===== 3. Speech-to-Text — Whisper =====

    /**
     * Transcribe an uploaded audio file to text.
     */
    public String transcribeAudio(MultipartFile audioFile) throws IOException {
        log.info("[STT] Transcribing audio: {}", audioFile.getOriginalFilename());
        Resource audioResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename();
            }
        };
        AudioTranscriptionResponse response = transcriptionModel.call(
                new AudioTranscriptionPrompt(audioResource)
        );
        return response.getResult().getOutput();
    }

    // ===== 4. Text-to-Speech =====

    /**
     * Convert text to speech audio. Returns raw MP3 bytes.
     */
    public byte[] synthesizeSpeech(String text) {
        log.info("[TTS] Synthesizing speech for: {}", truncate(text));
        SpeechResponse response = speechModel.call(new SpeechPrompt(text));
        return response.getResult().getOutput();
    }

    private String truncate(String text) {
        return text.length() > 60 ? text.substring(0, 60) + "..." : text;
    }
}
