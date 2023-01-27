package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.AnswerClassification;
import de.quantumrange.verbo.model.LearningMode;
import de.quantumrange.verbo.model.WordView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordViewRepository extends JpaRepository<WordView, Long> {

	@Query("""
			select count(distinct v) from view v
			where v.word.id = ?1 and v.owner.id = ?2 and v.reversed = ?3 and v.mode = ?4 and v.classification <> ?5""")
	long countLearned(long wordId, long userId, boolean reversed, LearningMode mode, AnswerClassification classification);

	@Query("select (count(v) > 0) from view v where v.word.id = ?1 and v.owner.id = ?2 and v.reversed = ?3")
	int countLearning(long wordId, long userId, boolean reversed);

	@Query("""
			select count(distinct v) from view v
			where v.word.owner.id = ?1 and v.owner.id = ?2 and v.reversed = ?3 and v.mode = ?4 and v.classification <> ?5""")
	long countGlobalLearned(long setId, long userId, boolean reversed, LearningMode mode, AnswerClassification classification);

	@Query("select (count(v) > 0) from view v where v.word.owner.id = ?1 and v.owner.id = ?2 and v.reversed = ?3")
	long countGlobalLearning(long setId, long userId, boolean reversed);

	default boolean isLearned(long wordId, long userId, boolean reversed) {
		return countLearned(wordId, userId, reversed, LearningMode.TEXT, AnswerClassification.WRONG) >= 2;
	}

	default boolean isLearning(long wordId, long userId, boolean reversed) {
		return countLearning(wordId, userId, reversed) != 0L;
	}
	
	@Query("select count(v) from view v where v.word.owner.id = ?1")
	long countByUserId(long id);
	
	@Query(value = "select v from view v where v.owner.id = ?1 and v.word.id = ?2 order by v.timestamp")
	Page<WordView> findWordViewsByUser(long userId, long wordId, Pageable pageable);
	
}
