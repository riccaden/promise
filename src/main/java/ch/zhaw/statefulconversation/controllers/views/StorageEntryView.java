package ch.zhaw.statefulconversation.controllers.views;

/**
 * Response-DTO fuer ein einzelnes Key-Value-Paar aus dem Agent-Storage.
 * Wird vom /storage-Endpoint als Liste zurueckgegeben (z.B. Block-Zusammenfassungen).
 */
public class StorageEntryView {
    private String key;
    private String value;

    public StorageEntryView(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}
