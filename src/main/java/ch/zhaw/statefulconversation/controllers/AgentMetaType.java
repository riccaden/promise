package ch.zhaw.statefulconversation.controllers;

/**
 * Enum fuer die verfuegbaren Agent-Typen im Oblivio-System.
 *
 * Jeder Typ hat einen numerischen Wert, der im DTO-Feld {@code type}
 * vom Frontend mitgesendet wird, um den gewuenschten Agent-Typ zu identifizieren.
 * - singleState (0): Legacy-Agent mit einem einzelnen State
 * - aurestaurant (1): Reserviert / Legacy-Typ
 * - biographer (2): Oblivio-Biographer mit 20-State-Kette
 */
public enum AgentMetaType {
    singleState(0),
    aurestaurant(1),
    biographer(2);

    private final int value;

    AgentMetaType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}