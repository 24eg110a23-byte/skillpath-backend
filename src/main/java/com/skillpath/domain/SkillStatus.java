package com.skillpath.domain;

/**
 * Derived status of a skill, calculated from its current and target level.
 * It is never stored on the entity, only exposed through API responses.
 */
public enum SkillStatus {

    /** Learner is still at the entry point of the ladder. */
    NOT_STARTED,
    /** Learner has moved past the entry point but has not reached the target. */
    IN_PROGRESS,
    /** Learner has reached (or exceeded) the target level. */
    ACHIEVED;

    public static SkillStatus from(SkillLevel current, SkillLevel target) {
        if (current == null || target == null) {
            return NOT_STARTED;
        }
        if (current.getRank() >= target.getRank()) {
            return ACHIEVED;
        }
        return current.getRank() > 1 ? IN_PROGRESS : NOT_STARTED;
    }
}
