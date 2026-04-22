package ch.zhaw.statefulconversation.model;

import java.util.function.Supplier;

/**
 * Hilfsklasse fuer Test-Assertions im PROMISE Framework.
 * Implementiert {@link Supplier}&lt;String&gt; und liefert eine lesbare Darstellung
 * eines beliebigen Objekts (Klassenname + toString). Wird als Message-Supplier
 * in JUnit-Assertions verwendet, um bei fehlgeschlagenen Tests den tatsaechlichen
 * Wert diagnostisch auszugeben.
 */
public class ObjectSerialisationSupplier implements Supplier<String> {

    private Object objectToBeSerialised;

    public ObjectSerialisationSupplier(Object objectToBeSerialised) {
        this.objectToBeSerialised = objectToBeSerialised;
    }

    @Override
    public String get() {
        return this.objectToBeSerialised.getClass() + " " + this.objectToBeSerialised.toString();
    }

}
