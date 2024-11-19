package org.estg.ipp.pt.Classes.Enum;

public enum TagType {
    INFO("General informational messages."),
    ERROR("Errors that require attention."),
    ALERT("Critical alerts that might indicate a major issue."),
    CRITICAL("Severe issues requiring immediate attention."),
    SUCCESS("Indicates successful operations."),
    FAILURE("Indicates unsuccessful operations."),
    ACCESS("Logs related to access control or attempts."),
    USER_ACTION("Logs of user actions or inputs."),
    DATABASE("Logs related to database operations."),
    NETWORK("Network-related logs (e.g., connection issues)."),
    SECURITY("Security-related logs (e.g., unauthorized access).");

    private final String description;

    // Constructor to set the description for each tag
    TagType(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the tag.
     *
     * @return The tag's description.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name();
    }
}
