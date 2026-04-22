package ch.zhaw.statefulconversation.model;

/**
 * Exception, die bei einem Zustandsuebergang im PROMISE Framework geworfen wird.
 *
 * Wird von {@link State#raiseIfTransit()} ausgeloest, wenn eine {@link Transition}
 * erfolgreich feuert. Der {@link Agent#respond(String)} faengt diese Exception ab
 * und wechselt daraufhin den aktuellen State auf den in der Exception enthaltenen
 * Folgezustand ({@code subsequentState}).
 *
 * Dieses Pattern (Exception als Kontrollfluss) ermoeglicht es, Zustandswechsel
 * aus tief verschachtelten Aufrufen heraus zu signalisieren, ohne den Rueckgabetyp
 * aller beteiligten Methoden zu veraendern.
 *
 * @see State#raiseIfTransit()
 * @see Agent#respond(String)
 */
public class TransitionException extends Exception {

    private State subsequentState;

    public TransitionException(State subsequentState) {
        this.subsequentState = subsequentState;
    }

    public State getSubsequentState() {
        return this.subsequentState;
    }
}
