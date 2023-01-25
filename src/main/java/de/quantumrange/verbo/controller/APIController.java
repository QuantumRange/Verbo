package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.*;
import eu.bitwalker.useragentutils.UserAgent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class APIController {
	
	private final UserService userService;
	private final VocSetService vocSetService;
	private final VocService vocService;
	private final VocDetailService vocDetailService;
	
	@Autowired
	public APIController(UserService userService,
	                     VocSetService vocSetService,
	                     VocService vocService,
	                     VocDetailService vocDetailService) {
		this.userService = userService;
		this.vocSetService = vocSetService;
		this.vocService = vocService;
		this.vocDetailService = vocDetailService;
	}
	
	@PostMapping(path = "set/mark")
	@PreAuthorize("hasAnyAuthority('api:set:mark')")
	public boolean mark(Principal principal,
	                    @RequestParam(name = "id") String id) {
		User user = userService.findByPrinciple(principal);
		VocSet set = vocSetService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalArgumentException("Invalid Set ID!"));
		
		if (user.getMarked().contains(set.getId())) {
			user.getMarked().remove(set.getId());
		} else {
			user.getMarked().add(set.getId());
		}
		
		userService.update(user);
		
		return true;
	}
	
	@PostMapping(path = "set")
	@PreAuthorize("hasAnyAuthority('api:basic')")
	public TSVocSet requestSet(Principal principal,
	                           @RequestBody String id) {
		User user = userService.findByPrinciple(principal);
		VocSet set = vocSetService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalArgumentException("Invalid Set ID!"));
		
		TSVocSet tsSet = new TSVocSet(Long.toString(set.getId()),
				set.getName(),
				set.getOwner(),
				new ArrayList<>());
		
		for (Long vocabulary : set.getVocabularies()) {
			Voc voc = vocService.findByID(vocabulary)
					.orElseThrow();
			
			tsSet.vocabularies().add(getVoc(user, voc));
		}
		
		return tsSet;
	}
	
	@PostMapping(path = "voc")
	@PreAuthorize("hasAnyAuthority('api:basic')")
	public TSVoc requestVoc(Principal principal,
	                        @RequestBody String id) {
		User user = userService.findByPrinciple(principal);
		Voc voc = vocService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalArgumentException("Invalid Set ID!"));
		
		return getVoc(user, voc);
	}
	
	@PostMapping(path = "voc/delete")
	@PreAuthorize("hasAnyAuthority('api:basic')")
	public boolean requestVocDelete(Principal principal,
	                                @RequestBody Map<String, String> data) {
		User user = userService.findByPrinciple(principal);
		VocSet set = vocSetService.findByID(Identifiable.getId(data.get("set")))
				.orElseThrow(() -> new IllegalArgumentException("Invalid set ID!"));
		Voc voc = vocService.findByID(Identifiable.getId(data.get("voc")))
				.orElseThrow(() -> new IllegalArgumentException("Invalid voc ID!"));
		
		if (set.getOwner() != user.getId())
			throw new IllegalStateException("Only owner edits allowed");
		
		set.getVocabularies().remove(voc.getId());
		vocSetService.update(set);
		
		return true;
	}
	
	@NotNull
	private TSVoc getVoc(User user, Voc voc) {
		return new TSVoc(Long.toString(voc.getId()),
				voc.getQuestion(),
				voc.getAnswer(),
				voc.getAnswerLang(),
				voc.getQuestionLang(),
				vocDetailService.findViewBy(user.getId(), voc.getId())
						.map(vi -> vi.getViews().stream()
								.map(view -> new TSVocView(view.timestamp(),
										view.answer(),
										view.correctness(),
										view.classification().ordinal(),
										view.mode().ordinal(),
										view.answerDuration(),
										view.reversed()))
								.sorted((o1, o2) -> Long.compare(o2.timestamp, o1.timestamp))
								.limit(5)
								.toList())
						
						.orElseGet(Collections::emptyList));
	}
	
	@PostMapping("learn")
	@PreAuthorize("hasAnyAuthority('site:learn')")
	@ResponseBody
	public boolean learnResponse(@RequestHeader(value = HttpHeaders.USER_AGENT) String userAgent,
	                             Principal principal,
	                             @RequestBody TSLearnResult result) {
		User user = userService.findByPrinciple(principal);
		Voc voc = vocService.findByID(Long.parseLong(result.snowflake))
				.orElseThrow();
		
		UserAgent ua = UserAgent.parseUserAgentString(userAgent);
		
		vocDetailService.insert(user.getId(),
				Long.parseLong(result.snowflake()),
				new VocView(result.timestamp(),
						result.answer(),
						PasswordService.damerauLevenshteinDistance(
								result.answer,
								result.reversed ? voc.getQuestion() : voc.getAnswer()),
						AnswerClassification.values()[result.classification()],
						result.responseTime(),
						LearningMode.values()[result.mode()],
						ua.getBrowser(),
						ua.getOperatingSystem(),
						result.reversed()));
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
