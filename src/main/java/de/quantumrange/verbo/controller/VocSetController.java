package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.ControlService.MenuID;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordRepository;
import de.quantumrange.verbo.service.repos.WordSetRepository;
import de.quantumrange.verbo.service.repos.WordViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("set")
public class VocSetController {
	
	private static final Logger log = LoggerFactory.getLogger(VocSetController.class);
	
	private final WordSetRepository wordSetRepository;
	private final UserRepository userRepository;
	private final WordRepository wordRepository;
	private final WordViewRepository wordViewRepository;
	private final ControlService controlService;
	
	public VocSetController(WordSetRepository wordSetRepository,
	                        UserRepository userRepository,
	                        WordRepository wordRepository,
	                        WordViewRepository wordViewRepository,
	                        ControlService controlService) {
		this.wordSetRepository = wordSetRepository;
		this.userRepository = userRepository;
		this.wordRepository = wordRepository;
		this.wordViewRepository = wordViewRepository;
		this.controlService = controlService;
	}
	
	@PostMapping("{set}/delete")
	@PreAuthorize("hasAnyAuthority('site:set')")
	@ResponseBody
	public boolean deleteWord(Principal principal,
	                          @PathVariable(name = "set") String setStr,
	                          @RequestBody String wordIdStr) {
		User user = userRepository.findByPrinciple(principal).orElseThrow();
		WordSet set = wordSetRepository.findById(Identifiable.getId(setStr))
				.orElseThrow();
		
		if (user.getId() != set.getOwner().getId()) return false;
		
		long wordId = Identifiable.getId(wordIdStr);
		
		log.warn("{} deleted in {} the word {}", user, set, wordRepository.getById(wordId));
		wordRepository.deleteById(wordId);
		
		return true;
	}
	
	@GetMapping("{id}")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String wordSet(Principal principal,
	                      Model model,
	                      @PathVariable(name = "id") String id) {
		User user = controlService.getUser(principal, model, MenuID.SET)
				.orElseThrow();
		WordSet wordSet = wordSetRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		
		
		Map<Word, String> progress = new HashMap<>();
		Map<Word, String> progressReversed = new HashMap<>();
		
		for (Word word : wordSet.getWords()) {
			if (isLearning(word.getId(), user.getId(), false)) {
				progress.put(word, isLearned(word.getId(), user.getId(), false)
						? "text-bg-success" : "text-bg-warning");
			}
			
			if (isLearning(word.getId(), user.getId(), true)) {
				progressReversed.put(word, isLearned(word.getId(), user.getId(), true)
						? "text-bg-success" : "text-bg-warning");
			}
		}
		
		model.addAttribute("words", wordSet.getWords());
		model.addAttribute("left", wordSet.getQuestion());
		model.addAttribute("right", wordSet.getAnswer());
		model.addAttribute("set", wordSet);
		model.addAttribute("creatorName", user.getDisplayName());
		
		model.addAttribute("progress", progress);
		model.addAttribute("progressReversed", progressReversed);
		
		return "vocSet";
	}
	
	private boolean isLearned(long wordId, long userId, boolean reversed) {
		return wordViewRepository.countLearned(wordId, userId, reversed, LearningMode.TEXT, AnswerClassification.WRONG) >= 2;
	}
	
	private boolean isLearning(long wordId, long userId, boolean reversed) {
		return wordViewRepository.countLearning(wordId, userId, reversed);
	}
	
	@GetMapping("{id}/delete")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String deleteWordSet(Principal principal,
	                            Model model,
	                            @PathVariable(name = "id") String id) {
		User user = controlService.getUser(principal, model, MenuID.SET)
				.orElseThrow();
		WordSet wordSet = wordSetRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		
		model.addAttribute("set", wordSet);
		log.info("{} tries to delete word-set {}", user, wordSet);
		
		return "deleteSet";
	}
	
	@PostMapping("{id}/deleteConfirm")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String deleteConfirm(Principal principal,
	                            @PathVariable(name = "id") String id) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		WordSet set = wordSetRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		
		if (set.getOwner().getId() != user.getId()) {
			log.warn("{} accessed set/{id}/deleteConfirm without being the owner!", user);
			return "redirect:/logout";
		}
		
		log.warn("{} confirmed deleted word-set {}", user, set);
		wordSetRepository.delete(set);
		
		return "redirect:/sets";
	}
	
	@GetMapping("import")
	@PreAuthorize("hasAnyAuthority('site:import')")
	public String importWordSet(Principal principal,
	                            Model model) {
		User user = controlService.getUser(principal, model, MenuID.SET)
				.orElseThrow();
		
		model.addAttribute("languages", Language.values());
		
		return "importFile";
	}
	
	@PostMapping("/import")
	@PreAuthorize("hasAnyAuthority('site:import')")
	public void uploadFile(Principal principal,
	                       @RequestParam("file") MultipartFile file,
	                       @RequestParam("name") String name,
	                       @RequestParam("langLeft") String langLeft,
	                       @RequestParam("langRight") String langRight,
	                       HttpServletResponse response) throws IOException {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		
		if (file.isEmpty()) {
			response.sendRedirect("/set/import");
			return;
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			
			final List<String> lines = new ArrayList<>();
			String line;
			
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			
			if (wordSetRepository.existsByName(name)) {
				response.sendRedirect("/set/import");
				// TODO: Send error code to user
				return;
			}
			
			Language left = Language.valueOf(langLeft);
			Language right = Language.valueOf(langRight);
			
			WordSet wordSet = wordSetRepository.save(new WordSet(0L,
					name,
					user,
					new HashSet<>(),
					new HashSet<>(),
					left,
					right,
					EditPolicy.OWNER,
					LocalDateTime.now(),
					new HashSet<>()));
			
			for (String l : lines) {
				String[] split = l.split(";");
				if (split.length != 2) continue;
				
				Word v = new Word(0L,
						wordSet,
						split[0],
						split[1]);
				
				wordSet.getWords().add(wordRepository.save(v));
			}
			
			wordSetRepository.saveAndFlush(wordSet);
			wordRepository.flush();
			
			response.sendRedirect("/set/" + wordSet.getVisibleId() + "/");
		} catch (IOException e) {
			log.error("Error during experimental import!", e);
		}
	}
	
}
