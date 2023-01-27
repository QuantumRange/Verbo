package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.AnswerClassification;
import de.quantumrange.verbo.model.LearningMode;
import de.quantumrange.verbo.model.WordView;
import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.List;

public interface WordViewRepository extends JpaRepository<WordView, Long> {

	@Query("""
			select count(distinct v) from view v
			where v.word.id = ?1 and v.owner.id = ?2 and v.reversed = ?3 and v.mode = ?4 and v.classification <> ?5""")
	long countLearned(long wordId, long userId, boolean reversed, LearningMode mode, AnswerClassification classification);

	@Query("select (count(v) > 0) from view v where v.word.id = ?1 and v.owner.id = ?2 and v.reversed = ?3")
	boolean countLearning(long wordId, long userId, boolean reversed);
	
	@Query("select count(v) from view v where v.word.owner.id = ?1")
	long countByUserId(long id);
	
	@Query(value = "select v from view v where v.owner.id = ?1 and v.word.id = ?2 order by v.timestamp")
	Page<WordView> findWordViewsByUser(long userId, long wordId, Pageable pageable);
	
}
