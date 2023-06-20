package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {

}
