package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordRepository;
import de.quantumrange.verbo.service.repos.WordSetRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("learn")
public class LearnController {
	
	private final WordSetRepository wordSetRepository;
	private final ControlService controlService;
	
	public LearnController(WordSetRepository wordSetRepository,
	                       ControlService controlService) {
		this.wordSetRepository = wordSetRepository;
		this.controlService = controlService;
	}
	
	@GetMapping("{id}")
	@PreAuthorize("hasAnyAuthority('site:learn')")
	public String create(Principal principal,
	                     Model model,
	                     @PathVariable String id,
	                     @RequestParam(value = "mode", defaultValue = "text") String mode) throws IOException {
		User user = controlService.getUser(principal, model, ControlService.MenuID.SET)
				.orElseThrow();
		if (user.get(MetaKey.FORCE_PASSWORD_CHANGE).equals("true")) return "redirect:/myAccount";
		
		WordSet set = wordSetRepository.findById(Identifiable.getId(id))
				.orElseThrow();
		String[] modes = mode.split(" ");
		
		Set<LearningMode> types = new HashSet<>();
		
		for (String s : modes) {
			LearningMode type = null;
			
			for (LearningMode value : LearningMode.values()) {
				if (value.name().equalsIgnoreCase(s)) {
					type = value;
					break;
				}
			}
			
			if (type != null) types.add(type);
		}
		
		model.addAttribute("set", set.getVisibleId());
		model.addAttribute("options", types.stream()
				.map(Enum::ordinal)
				.map(i -> Integer.toString(i))
				.collect(Collectors.joining(";")));
		
		return "learn";
	}
	
}
