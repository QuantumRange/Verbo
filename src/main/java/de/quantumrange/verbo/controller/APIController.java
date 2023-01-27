package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.CommonPasswordDetectionService;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordRepository;
import de.quantumrange.verbo.service.repos.WordSetRepository;
import de.quantumrange.verbo.service.repos.WordViewRepository;
import de.quantumrange.verbo.util.StringUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class APIController {
	
	private final UserRepository userRepository;
	private final WordSetRepository wordSetRepository;
	private final WordRepository wordRepository;
	private final WordViewRepository wordViewRepository;
	
	@Autowired
	public APIController(UserRepository userRepository,
	                     WordSetRepository wordSetRepository,
	                     WordRepository wordRepository,
	                     WordViewRepository wordViewRepository) {
		this.userRepository = userRepository;
		this.wordSetRepository = wordSetRepository;
		this.wordRepository = wordRepository;
		this.wordViewRepository = wordViewRepository;
	}
	
	@PostMapping(path = "set/mark")
	@PreAuthorize("hasAnyAuthority('api:set:mark')")
	public boolean mark(Principal principal,
	                    @RequestParam(name = "id") String id) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		WordSet set = wordSetRepository.findById(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalArgumentException("Invalid Set ID!"));
		
		if (user.getMarked().contains(set)) {
			user.getMarked().remove(set);
		} else {
			user.getMarked().add(set);
		}
		
		userRepository.save(user);
		
		return true;
	}
	
	@PostMapping(path = "set")
	@PreAuthorize("hasAnyAuthority('api:basic')")
	public TSVocSet requestSet(Principal principal,
	                           @RequestBody String id) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		WordSet set = wordSetRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		
		TSVocSet tsSet = new TSVocSet(Long.toString(set.getId()),
				set.getName(),
				set.getOwner().getId(),
				new ArrayList<>());
		
		for (Word word : set.getWords()) {
			tsSet.vocabularies().add(getVoc(user, word));
		}
		
		return tsSet;
	}
	
	@PostMapping(path = "voc")
	@PreAuthorize("hasAnyAuthority('api:basic')")
	public TSVoc requestVoc(Principal principal,
	                        @RequestBody String id) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		Word word = wordRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		
		return getVoc(user, word);
	}
	
	@PostMapping(path = "voc/delete")
	@PreAuthorize("hasAnyAuthority('api:basic')")
	public boolean requestVocDelete(Principal principal,
	                                @RequestBody Map<String, String> data) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		WordSet set = wordSetRepository.findById(Identifiable.getId(data.get("set")))
				.orElseThrow();
		Word word = wordRepository.findById(Identifiable.getId(data.get("voc")))
				.orElseThrow();
		
		if (set.getOwner().getId() != user.getId())
			throw new IllegalStateException("Only owner edits allowed");
		
		set.getWords().remove(word);
		wordSetRepository.save(set);
		
		return true;
	}
	
	@NotNull
	private TSVoc getVoc(User user, Word word) {
		return new TSVoc(Long.toString(word.getId()),
				word.getQuestion(),
				word.getAnswer(),
				word.getOwner().getQuestion(),
				word.getOwner().getAnswer(),
				wordViewRepository.findWordViewsByUser(user.getId(), word.getId(), PageRequest.of(1, 5))
						.map(view -> new TSVocView(view.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
								view.getAnswer(),
								view.getCorrectness(),
								view.getClassification().ordinal(),
								view.getMode().ordinal(),
								view.getAnswerDuration(),
								view.isReversed()))
						.toList());
	}
	
	@PostMapping("learn")
	@PreAuthorize("hasAnyAuthority('site:learn')")
	@ResponseBody
	public boolean learnResponse(Principal principal,
	                             @RequestBody TSLearnResult result) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		Word word = wordRepository.findById(Long.parseLong(result.snowflake))
				.orElseThrow();
		
		wordViewRepository.save(new WordView(
				0L,
				word,
				user,
				LocalDateTime.now(),
				result.answer,
				StringUtil.damerauLevenshteinDistance(
						result.answer,
						result.reversed ? word.getQuestion() : word.getAnswer()),
				AnswerClassification.values()[result.classification()],
				result.responseTime(),
				LearningMode.values()[result.mode()],
				result.reversed
		));

		return true;
	}
	
	record TSVoc(String id,
	             String question,
	             String answer,
	             Language questionLanguage,
	             Language answerLanguage,
	             List<TSVocView> views) {
	}
	
	record TSVocView(long timestamp,
	                 String answer,
	                 int correctness,
	                 int classification,
	                 int responseTime,
	                 int mode,
	                 boolean reversed) {
	}
	
	record TSVocSet(String id,
	                String name,
	                long owner,
	                List<TSVoc> vocabularies) {
	}
	
	record TSLearnResult(String snowflake,
	                     long timestamp,
	                     String answer,
	                     int classification,
	                     int mode,
	                     int responseTime,
	                     boolean reversed) {
	}
	
}
