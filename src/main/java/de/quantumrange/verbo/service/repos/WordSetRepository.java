package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.WordSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface WordSetRepository extends JpaRepository<WordSet, Long> {

	@Query("select ws from word_set ws where ws.id = ?1")
	Optional<WordSet> findById(long id);



}
