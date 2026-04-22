package ch.zhaw.statefulconversation.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Hilfsklasse fuer String-Formatierung mit benannten Platzhaltern.
 *
 * <p>Ersetzt {@code ${name}}-Platzhalter in Template-Strings durch Werte aus einer
 * {@link Map}{@code <String, JsonElement>}. Wird von dynamischen State-Klassen verwendet,
 * um zur Laufzeit extrahierte Daten in Prompt-Templates einzusetzen.
 *
 * @see <a href="https://www.baeldung.com/java-string-formatting-named-placeholders">Baeldung-Referenz</a>
 */
public class NamedParametersFormatter {

    /**
     * Ersetzt alle {@code ${key}}-Platzhalter im Template durch die entsprechenden Werte.
     * Bei {@link JsonPrimitive}-Werten werden umschliessende Anfuehrungszeichen entfernt.
     */
    public static String format(String template, Map<String, JsonElement> parameters) {
        StringBuilder newTemplate = new StringBuilder(template);
        List<Object> valueList = new ArrayList<>();

        Matcher matcher = Pattern.compile("[$][{](\\w+)}").matcher(template);

        String currentValue;
        while (matcher.find()) {
            String key = matcher.group(1);

            String paramName = "${" + key + "}";
            int index = newTemplate.indexOf(paramName);
            if (index != -1) {
                newTemplate.replace(index, index + paramName.length(), "%s");
                currentValue = parameters.get(key).toString();
                if (parameters.get(key) instanceof JsonPrimitive) {
                    currentValue = currentValue.replaceAll("^\"|\"$", "");
                }
                valueList.add(currentValue); // added this .toString() to turn lists into [.., .., ..]
            }
        }

        return String.format(newTemplate.toString(), valueList.toArray());
    }
}