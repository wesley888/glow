package com.glow.service.output;

import com.glow.model.MovieRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Module 3 — Structured Output.
 * <p>
 * Spring AI automatically appends the JSON schema to the prompt and
 * deserializes the LLM response. Demonstrates four output strategies:
 * <ol>
 *   <li>{@code entity(Class)} — single Java record/class</li>
 *   <li>{@code entity(ParameterizedTypeReference)} — generic list of records</li>
 *   <li>{@link ListOutputConverter} — list of strings</li>
 *   <li>{@link MapOutputConverter} — key-value map</li>
 * </ol>
 */
@Service
public class StructuredOutputService {

    private final ChatClient chatClient;

    public StructuredOutputService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 1. Single entity — LLM returns one MovieRecommendation as JSON.
     */
    public MovieRecommendation recommendMovie(String genre) {
        return chatClient.prompt()
                .user("Recommend one great " + genre + " movie. Return complete details.")
                .call()
                .entity(MovieRecommendation.class);
    }

    /**
     * 2. List of entities — LLM returns a JSON array of MovieRecommendations.
     */
    public List<MovieRecommendation> recommendMovies(String genre, int count) {
        return chatClient.prompt()
                .user("Recommend %d great %s movies. Return as a JSON array.".formatted(count, genre))
                .call()
                .entity(new ParameterizedTypeReference<List<MovieRecommendation>>() {});
    }

    /**
     * 3. ListOutputConverter — LLM returns a comma-separated list, parsed to List<String>.
     */
    public List<String> listTopics(String subject) {
        ListOutputConverter converter = new ListOutputConverter(new DefaultConversionService());
        String format = converter.getFormat();

        String response = chatClient.prompt()
                .user("List 5 key topics in %s. %s".formatted(subject, format))
                .call()
                .content();

        return converter.convert(response);
    }

    /**
     * 4. MapOutputConverter — LLM returns key-value pairs (e.g. country capitals).
     */
    public Map<String, Object> getCountryInfo(String country) {
        MapOutputConverter converter = new MapOutputConverter();
        String format = converter.getFormat();

        String response = chatClient.prompt()
                .user("Provide information about %s: capital, population, currency, language. %s"
                        .formatted(country, format))
                .call()
                .content();

        return converter.convert(response);
    }

    /**
     * 5. BeanOutputConverter with explicit schema — maximum control over the output format.
     */
    public MovieRecommendation recommendMovieWithConverter(String genre) {
        BeanOutputConverter<MovieRecommendation> converter =
                new BeanOutputConverter<>(MovieRecommendation.class);

        String response = chatClient.prompt()
                .user("Recommend a %s movie. %s".formatted(genre, converter.getFormat()))
                .call()
                .content();

        return converter.convert(response);
    }
}
