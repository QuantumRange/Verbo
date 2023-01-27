package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.Course;
import de.quantumrange.verbo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface CourseRepository extends JpaRepository<Course, Long> {
	@Query("select (count(c) > 0) from course c where upper(c.name) = upper(?1)")
	boolean existsByNameAllIgnoreCase(String name);

	@Query("select c from course c inner join c.users users where users.id = ?1")
	Set<Course> findByWatcher(long id);
	
	@Query("select c from course c where c.code = ?1")
	Optional<Course> findByCode(String code);

}
