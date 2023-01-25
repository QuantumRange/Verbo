package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.*;
import de.quantumrange.verbo.service.ControlService.MenuID;
import org.apache.log4j.Logger;
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
import java.util.stream.Collectors;

import static de.quantumrange.verbo.model.VocSet.SAVE_FORMAT;

@Controller
@RequestMapping("set")
public class VocSetController {

	private static final Logger log = Logger.getLogger(VocSetController.class);

	private final VocSetService vocSetService;
	private final UserService userService;
	private final VocService vocService;
	private final VocDetailService vocDetailService;
	private final ControlService controlService;

	public VocSetController(VocSetService vocSetService, UserService userService, VocService vocService, VocDetailService vocDetailService, ControlService controlService) {
		this.vocSetService = vocSetService;
		this.userService = userService;
		this.vocService = vocService;
		this.vocDetailService = vocDetailService;
		this.controlService = controlService;
	}

	@PostMapping("{set}/delete")
	@PreAuthorize("hasAnyAuthority('site:set')")
	@ResponseBody
	public boolean deleteVoc(Principal principal,
							 @PathVariable(name = "set") String setStr,
							 @RequestBody String vocStr) {
		User user = userService.findByPrinciple(principal);
		VocSet set = vocSetService.findByID(Identifiable.getId(setStr))
				.orElseThrow(() -> new IllegalStateException("Voc ID is invalid!"));
		long vocId = Identifiable.getId(vocStr);

		if (set.getOwner() == user.getId()) {
			set.getVocabularies().remove(vocId);

			vocSetService.update(set);
		}

		return true;
	}

	@GetMapping("{id}")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String view(Principal principal,
					   Model model,
					   @PathVariable(name = "id") String id) {
		User user = controlService.getUser(principal, model, MenuID.SET);
		VocSet set = vocSetService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalStateException("Voc ID is invalid!"));

		model.addAttribute("set", set);
		model.addAttribute("creatorName", userService.findByID(set.getOwner())
				.map(User::getDisplayName)
				.orElse("N/A"));

		Set<Voc> vocs = set.getVocabularies().stream()
				.map(vocService::findByID)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());


		Map<Long, VocInfo> map = vocDetailService.findViewBy(user.getId())
				.orElseGet(Map::of);

		Map<Voc, String> progress = new HashMap<>();
		Map<Voc, String> progressReversed = new HashMap<>();

		for (Voc voc : vocs) {
			VocInfo info = map.getOrDefault(voc.getId(), null);

			if (info == null) continue;

			// TODO: Remove duplicate line
			if (hasViews(info, false))
				progress.put(voc, isLearned(info, false) ? "text-bg-success" : "text-bg-warning");
			if (hasViews(info, true))
				progressReversed.put(voc, isLearned(info, true) ? "text-bg-success" : "text-bg-warning");
		}

		model.addAttribute("vocabularies", vocs);
		model.addAttribute("left", vocs.stream().findFirst()
				.map(Voc::getQuestionLang)
				.orElse(Language.ENGLISH));
		model.addAttribute("right", vocs.stream().findFirst()
				.map(Voc::getAnswerLang)
				.orElse(Language.ENGLISH));

		inject(model, "default_", vocs, map, false);
		inject(model, "reversed_", vocs, map, true);

		model.addAttribute("progress", progress);
		model.addAttribute("progressReversed", progressReversed);

		return "vocSet";
	}

	private boolean isLearned(VocInfo voc, boolean reversed) {
		return voc.getViews().stream()
				.filter(view -> view.reversed() == reversed)
				.sorted((o1, o2) -> Long.compare(o2.timestamp(), o1.timestamp())) // TODO: Bug
				.limit(3)
				.filter(v -> v.classification().isCorrect() && v.mode() == LearningMode.TEXT)
				.count() == 3;
	}

	private boolean hasViews(VocInfo voc, boolean reversed) {
		return voc.getViews()
				.stream()
				.anyMatch(vocView -> vocView.reversed() == reversed);
	}

	private void inject(Model model,
						String prefix,
						Set<Voc> vocs,
						Map<Long, VocInfo> map,
						boolean reversed) {
		// three times in a row to count as learned
		double learned = ((double) (vocs.stream()
				.filter(voc -> map.containsKey(voc.getId()))
				.map(voc -> map.get(voc.getId()))
				.filter(voc -> isLearned(voc, reversed))
				.count()) / vocs.size()) * 100.;
		model.addAttribute(prefix + "vLearned", learned);

		double learning = (((double) (vocs.stream()
				.filter(voc -> map.containsKey(voc.getId()))
				.filter(voc -> hasViews(map.get(voc.getId()), reversed))
				.count()) / vocs.size()) * 100.);

		model.addAttribute(prefix + "vLearning", learning);

		double unknown = 100.0 - (learned + learning);
		model.addAttribute(prefix + "vUnknown", unknown);
	}

	@GetMapping("{id}/delete")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String delete(Principal principal,
						 Model model,
						 @PathVariable(name = "id") String id) {
		User user = controlService.getUser(principal, model, MenuID.SET);
		VocSet set = vocSetService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalStateException("Voc ID is invalid!"));

		model.addAttribute("set", set);

		return "deleteSet";
	}

	@PostMapping("{id}/deleteConfirm")
	@PreAuthorize("hasAnyAuthority('site:set')")
	public String deleteConfirm(Principal principal,
								@PathVariable(name = "id") String id) {
		User user = userService.findByPrinciple(principal);
		VocSet set = vocSetService.findByID(Identifiable.getId(id))
				.orElseThrow(() -> new IllegalStateException("Voc ID is invalid!"));

		if (set.getOwner() != user.getId()) {
			log.warn(user.getId() + user.toString() + " tries to access delete confirm!");
			throw new IllegalStateException("O.o what?");
		}

		vocSetService.remove(set);

		return "redirect:/sets";
	}

	@GetMapping("import")
	@PreAuthorize("hasAnyAuthority('site:import')")
	public String imp(Principal principal,
					  Model model) {
		User user = controlService.getUser(principal, model, MenuID.SET);

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
		User user = userService.findByPrinciple(principal);

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

			if (vocSetService.parallel()
					.anyMatch(v -> v.getName().equalsIgnoreCase(name))) {
				response.sendRedirect("/set/import");
				return;
			}

			System.out.println(langLeft);
			System.out.println(langRight);

			Language left = Language.valueOf(langLeft);
			Language right = Language.valueOf(langRight);

			VocSet set = new VocSet(vocSetService.generateID(),
					name,
					user.getId(),
					SAVE_FORMAT.format(LocalDateTime.now()),
					new HashSet<>());

			for (String l : lines) {
				String[] split = l.split(";");
				if (split.length != 2) continue;

				Voc v = new Voc(vocService.generateID(),
						split[0],
						left,
						split[1],
						right);

				vocService.insert(v);

				set.getVocabularies().add(v.getId());
			}

			vocSetService.insert(set);

			response.sendRedirect("/set/" + set.getVisibleId() + "/");

			// Path path = Paths.get(fileName);
			// Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
