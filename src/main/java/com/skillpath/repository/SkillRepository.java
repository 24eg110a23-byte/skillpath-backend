package com.skillpath.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillpath.domain.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Skill> findAllByOrderByNameAsc();

    @Query("""
            select distinct s from Skill s
            where (cast(:category as string) is null
                   or lower(s.category) = lower(cast(:category as string)))
              and (cast(:search as string) is null
                   or lower(s.name) like lower(concat('%', cast(:search as string), '%'))
                   or lower(s.category) like lower(concat('%', cast(:search as string), '%'))
                   or lower(coalesce(s.description, '')) like lower(concat('%', cast(:search as string), '%')))
            """)
    Page<Skill> search(
            @Param("search") String search,
            @Param("category") String category,
            Pageable pageable
    );

    @Query("""
            select distinct s.category from Skill s
            order by s.category asc
            """)
    List<String> findDistinctCategories();
}
