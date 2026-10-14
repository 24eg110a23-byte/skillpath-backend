package com.skillpath.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillpath.domain.Course;
import com.skillpath.domain.CourseStatus;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCase(String title);

    List<Course> findAllByOrderByTitleAsc();

    long countByStatus(CourseStatus status);

    @Query("""
            select distinct c from Course c
            where (:status is null or c.status = :status)
              and (cast(:search as string) is null
                   or lower(c.title) like lower(concat('%', cast(:search as string), '%'))
                   or lower(c.provider) like lower(concat('%', cast(:search as string), '%'))
                   or lower(coalesce(c.description, '')) like lower(concat('%', cast(:search as string), '%')))
            """)
    Page<Course> search(
            @Param("search") String search,
            @Param("status") CourseStatus status,
            Pageable pageable
    );

    @Query("""
            select distinct c.provider from Course c
            order by c.provider asc
            """)
    List<String> findDistinctProviders();

    @Query("select coalesce(sum(c.totalHours), 0) from Course c")
    long sumTotalHours();

    @Query("select coalesce(sum(c.completedHours), 0) from Course c")
    long sumCompletedHours();
}
