package com.skillpath.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillpath.domain.SkillCourseLink;

@Repository
public interface SkillCourseLinkRepository extends JpaRepository<SkillCourseLink, Long> {

    Optional<SkillCourseLink> findBySkillIdAndCourseId(Long skillId, Long courseId);

    boolean existsBySkillIdAndCourseId(Long skillId, Long courseId);

    List<SkillCourseLink> findAllBySkillId(Long skillId);

    List<SkillCourseLink> findAllByCourseId(Long courseId);

    void deleteBySkillId(Long skillId);

    void deleteByCourseId(Long courseId);

    @Query("select count(l) from SkillCourseLink l where l.skill.id = :skillId")
    long countBySkillId(@Param("skillId") Long skillId);

    @Query("select count(l) from SkillCourseLink l where l.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("""
            select l.skill.id as ownerId, count(l) as total
            from SkillCourseLink l
            group by l.skill.id
            """)
    List<OwnerCount> countGroupedBySkill();

    @Query("""
            select l.course.id as ownerId, count(l) as total
            from SkillCourseLink l
            group by l.course.id
            """)
    List<OwnerCount> countGroupedByCourse();

    /** Projection pairing an owning entity id with the number of links it has. */
    interface OwnerCount {

        Long getOwnerId();

        long getTotal();
    }
}
