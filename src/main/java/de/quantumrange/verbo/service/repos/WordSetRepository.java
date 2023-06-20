package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.WordSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WordSetRepository extends JpaRepository<WordSet, Long> {
	
	@Query("select ws from word_set ws where ws.id = ?1")
	Optional<WordSet> findById(long id);
	
	@Query("select (count(w) > 0) from word_set w where upper(w.name) = upper(?1)")
	boolean existsByName(String name);
	
	@Query("select ws from word_set ws where ws.owner.id = ?1 order by ws.name")
	List<WordSet> findWordSetsByOwner(long ownerId);
	
}
