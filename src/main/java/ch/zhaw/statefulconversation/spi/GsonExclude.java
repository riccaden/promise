package ch.zhaw.statefulconversation.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Markierungs-Annotation zum Ausschluss von Feldern bei der GSON-Serialisierung.
 *
 * <p>Wird typischerweise auf JPA-IDs und bidirektionale Referenzen angewendet,
 * um zirkulaere Serialisierung und unnoetige Daten im JSON zu vermeiden.
 * Die zugehoerige {@link com.google.gson.ExclusionStrategy} ist in {@link LMOpenAI} definiert.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GsonExclude {

}