package ch.zhaw.statefulconversation.controllers.views;

/**
 * Response-DTO fuer den aktuellen Zustand eines Agents.
 * Enthaelt den State-Namen sowie bei verschachtelten OuterStates
 * den inneren State-Namen und die gesamte innere State-Kette.
 */
public class AgentStateInfoView {
    private String name;
    private String innerName;
    private java.util.List<String> innerNames;

    public AgentStateInfoView(String name, String innerName, java.util.List<String> innerNames) {
        this.name = name;
        this.innerName = innerName;
        this.innerNames = innerNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInnerName() {
        return innerName;
    }

    public void setInnerName(String innerName) {
        this.innerName = innerName;
    }

    public java.util.List<String> getInnerNames() {
        return innerNames;
    }

    public void setInnerNames(java.util.List<String> innerNames) {
        this.innerNames = innerNames;
    }
}
