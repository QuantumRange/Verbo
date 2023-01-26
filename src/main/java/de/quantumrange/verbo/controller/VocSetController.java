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

import static de.quantumrange.verbo.model.WordSet.SAVE_FORMAT;

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

		log.info("{} deleted in {} the word {}", user, set, wordRepository.getById(wordId));
		wordRepository.deleteById(wordId);

		return true;
	}

	@GetMapping("{id}")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String getView(Principal principal,
						  Model model,
						  @PathVariable(name = "id") String id) {
		User user = controlService.getUser(principal, model, MenuID.SET)
				.orElseThrow();
		WordSet wordSet = wordSetRepository.findById(Identifiable.getId(id))
				.orElseThrow();

		model.addAttribute("set", wordSet);
		model.addAttribute("creatorName", user.getDisplayName());

		Map<Word, String> progress = new HashMap<>();
		Map<Word, String> progressReversed = new HashMap<>();

		for (Word word : wordSet.getWords()) {
			if (wordViewRepository.isLearning(word.getId(), user.getId(), false)) {
				progress.put(word, wordViewRepository.isLearned(word.getId(), user.getId(), false)
						? "text-bg-success" : "text-bg-warning");
			}

			if (wordViewRepository.isLearning(word.getId(), user.getId(), true)) {
				progressReversed.put(word, wordViewRepository.isLearned(word.getId(), user.getId(), true)
						? "text-bg-success" : "text-bg-warning");
			}
		}

		model.addAttribute("words", wordSet.getWords());
		model.addAttribute("left", wordSet.getQuestion());
		model.addAttribute("right", wordSet.getAnswer());

		inject(model, "default_", wordSet, false);
		inject(model, "reversed_", wordSet, true);

		model.addAttribute("progress", progress);
		model.addAttribute("progressReversed", progressReversed);

		return "vocSet";
	}

	private void inject(Model model,
						String prefix,
						WordSet set,
						boolean reversed) {
		// three times in a row to count as learned
		double learned = ((double) (words.stream()
				.filter(word -> map.containsKey(word.getId()))
				.map(word -> map.get(word.getId()))
				.filter(voc -> isLearned(voc, reversed))
				.count()) / words.size()) * 100.;
		model.addAttribute(prefix + "vLearned", learned);

		double learning = (((double) (words.stream()
				.filter(word -> map.containsKey(word.getId()))
				.filter(word -> hasViews(map.get(word.getId()), reversed))
				.count()) / words.size()) * 100.);

		model.addAttribute(prefix + "vLearning", learning);

		double unknown = 100.0 - (learned + learning);
		model.addAttribute(prefix + "vUnknown", unknown);
	}

	@GetMapping("{id}/delete")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String delete(Principal principal,
						 Model model,
						 @PathVariable(name = "id") String id) {
		Optional<User> user = controlService.getUser(principal, model, MenuID.SET);
		WordSet set = wordSetRepository.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalStateException("Voc ID is invalid!"));

		model.addAttribute("set", set);

		return "deleteSet";
	}

	@PostMapping("{id}/deleteConfirm")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String deleteConfirm(Principal principal,
								@PathVariable(name = "id") String id) {
		User user = userRepository.findByPrinciple(principal);
		WordSet set = wordSetRepository.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalStateException("Voc ID is invalid!"));

		if (set.getOwner() != user.getId()) {
			log.warn(user.getId() + user.toString() + " tries to access delete confirm!");
			throw new IllegalStateException("O.o what?");
		}

		wordSetRepository.remove(set);

		return "redirect:/sets";
	}

	@GetMapping("import")
	@PreAuthorize("hasAnyAuthority('site:import')")
	public String imp(Principal principal,
					  Model model) {
		Optional<User> user = controlService.getUser(principal, model, MenuID.SET);

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
		User user = userRepository.findByPrinciple(principal);

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

			if (wordSetRepository.parallel()
					.anyMatch(v -> v.getName().equalsIgnoreCase(name))) {
				response.sendRedirect("/set/import");
				return;
			}

			System.out.println(langLeft);
			System.out.println(langRight);

			Language left = Language.valueOf(langLeft);
			Language right = Language.valueOf(langRight);

			WordSet set = new WordSet(wordSetRepository.generateID(),
					name,
					user.getId(),
					SAVE_FORMAT.format(LocalDateTime.now()),
					new HashSet<>());

			for (String l : lines) {
				String[] split = l.split(";");
				if (split.length != 2) continue;

				Word v = new Word(wordRepository.generateID(),
						split[0],
						left,
						split[1],
						right);

				wordRepository.insert(v);

				set.getVocabularies().add(v.getId());
			}

			wordSetRepository.insert(set);

			response.sendRedirect("/set/" + set.getVisibleId() + "/");

			// Path path = Paths.get(fileName);
			// Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
