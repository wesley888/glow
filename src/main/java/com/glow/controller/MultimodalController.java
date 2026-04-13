package com.glow.controller;

import com.glow.service.multimodal.MultimodalService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Module 9 — Multimodal API.
 * <pre>
 * POST /api/multimodal/vision/describe   — describe an uploaded image
 * POST /api/multimodal/vision/analyze    — ask a question about an image
 * POST /api/multimodal/image/generate    — generate image from text (DALL-E)
 * POST /api/multimodal/audio/transcribe  — speech-to-text (Whisper)
 * POST /api/multimodal/audio/synthesize  — text-to-speech (TTS) → MP3 bytes
 * </pre>
 */
@RestController
@RequestMapping("/api/multimodal")
public class MultimodalController {

    private final MultimodalService multimodalService;

    public MultimodalController(MultimodalService multimodalService) {
        this.multimodalService = multimodalService;
    }

    // ===== Vision =====

    @PostMapping("/vision/describe")
    public String describeImage(@RequestParam("file") MultipartFile file) throws IOException {
        return multimodalService.describeImage(file);
    }

    @PostMapping("/vision/analyze")
    public String analyzeImage(@RequestParam("file") MultipartFile file,
                               @RequestParam String question) throws IOException {
        return multimodalService.analyzeImage(file, question);
    }

    // ===== Image Generation =====

    @PostMapping("/image/generate")
    public String generateImage(
            @RequestParam String prompt,
            @RequestParam(defaultValue = "1024x1024") String size,
            @RequestParam(defaultValue = "standard") String quality) {
        return multimodalService.generateImage(prompt, size, quality);
    }

    // ===== Speech-to-Text =====

    @PostMapping("/audio/transcribe")
    public String transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        return multimodalService.transcribeAudio(file);
    }

    // ===== Text-to-Speech =====

    @PostMapping("/audio/synthesize")
    public ResponseEntity<byte[]> synthesizeSpeech(@RequestParam String text) {
        byte[] audio = multimodalService.synthesizeSpeech(text);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=speech.mp3")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(audio);
    }
}
