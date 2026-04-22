package ch.zhaw.statefulconversation.spi;

/**
 * Wird geworfen, wenn die OpenAI API eine Antwort aufgrund des Content Filters blockiert.
 *
 * <p>Die Erkennung erfolgt ueber {@code finish_reason: "content_filter"} in der API-Antwort.
 */
public class ContenFilterException extends RuntimeException {
    public ContenFilterException(String message) {
        super(message);
    }

}
