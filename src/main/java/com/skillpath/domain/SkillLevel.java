package com.skillpath.domain;

/**
 * Proficiency ladder used for both the current and the target level of a {@link Skill}.
 * The {@link #rank} value gives a stable numeric ordering so progress can be computed.
 */
public enum SkillLevel {

    BEGINNER(1, "Beginner"),
    INTERMEDIATE(2, "Intermediate"),
    ADVANCED(3, "Advanced"),
    EXPERT(4, "Expert");

    private final int rank;
    private final String label;

    SkillLevel(int rank, String label) {
        this.rank = rank;
        this.label = label;
    }

    public int getRank() {
        return rank;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Completion of a skill expressed as a percentage of the target level.
     * A learner sitting on their target level is 100% complete.
     */
    public static int progressPercent(SkillLevel current, SkillLevel target) {
        if (current == null || target == null) {
            return 0;
        }
        int targetRank = Math.max(target.getRank(), 1);
        int raw = Math.round((current.getRank() * 100f) / targetRank);
        return Math.min(100, Math.max(0, raw));
    }
}
