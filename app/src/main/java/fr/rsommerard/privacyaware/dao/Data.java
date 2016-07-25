package fr.rsommerard.privacyaware.dao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "DATA".
 */
public class Data implements java.io.Serializable {

    private Long id;
    /** Not-null value. */
    private String content;
    /** Not-null value. */
    private String identifier;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Data() {
    }

    public Data(Long id) {
        this.id = id;
    }

    public Data(Long id, String content, String identifier) {
        this.id = id;
        this.content = content;
        this.identifier = identifier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getContent() {
        return content;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setContent(String content) {
        this.content = content;
    }

    /** Not-null value. */
    public String getIdentifier() {
        return identifier;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    // KEEP METHODS - put your custom methods here
    @Override
    public String toString() {
        return "{" + this.identifier + ", " + this.content + "}";
    }
    // KEEP METHODS END

}
