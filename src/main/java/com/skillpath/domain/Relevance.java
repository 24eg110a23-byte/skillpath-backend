package com.skillpath.domain;

/**
 * How strongly a {@link Course} contributes to a {@link Skill} it is linked to.
 */
public enum Relevance {

    ESSENTIAL("Essential"),
    HELPFUL("Helpful"),
    OPTIONAL("Optional");

    private final String label;

    Relevance(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
