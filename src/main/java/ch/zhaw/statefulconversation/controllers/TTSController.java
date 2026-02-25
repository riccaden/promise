package ch.zhaw.statefulconversation.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import ch.zhaw.statefulconversation.controllers.views.TTSRequest;

/**
 * Controller for Text-to-Speech functionality using ElevenLabs API
 */
@RestController
public class TTSController {

    @Value("${elevenlabs.api.key:}")
    private String apiKey;

    @Value("${elevenlabs.voice.id:pNInz6obpgDQGcFmaJgB}")
    private String voiceId;

    private final RestTemplate restTemplate;

    public TTSController() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Convert text to speech using ElevenLabs API
     *
     * @param agentID Agent ID (not used, but kept for consistency with other endpoints)
     * @param request TTSRequest containing the text to convert
     * @return Audio data as byte array
     */
    @PostMapping("{agentID}/tts")
    public ResponseEntity<byte[]> textToSpeech(@PathVariable String agentID, @RequestBody TTSRequest request) {

        // Validate API key
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_ELEVENLABS_API_KEY_HERE")) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }

        // Validate request
        if (request == null || request.getText() == null || request.getText().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            // Prepare ElevenLabs API request
            // output_format is a query parameter, not a body field
            String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId + "?output_format=mp3_44100_128";

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "audio/mpeg");
            headers.set("xi-api-key", apiKey);

            // Prepare request body
            Map<String, Object> requestBody = Map.of(
                "text", request.getText(),
                "model_id", "eleven_multilingual_v2",
                "voice_settings", Map.of(
                    "stability", 0.5,
                    "similarity_boost", 0.75
                )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Call ElevenLabs API
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                byte[].class
            );

            // Return audio data
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType("audio/mpeg"));

            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("ElevenLabs API error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("Error calling ElevenLabs API: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
