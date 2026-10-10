package com.skillpath.domain;

/**
 * Lifecycle of a {@link Course} from the learner's point of view.
 */
public enum CourseStatus {

    NOT_STARTED("Not started"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed");

    private final String label;

    CourseStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
