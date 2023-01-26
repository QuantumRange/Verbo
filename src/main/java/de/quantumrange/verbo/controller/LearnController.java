package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.UserService;
import de.quantumrange.verbo.service.VocService;
import de.quantumrange.verbo.service.VocSetService;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("learn")
public class LearnController {
	
	public static final int MAX_SESSION_SIZE = 6;
	
	private final VocSetService vocSetService;
	private final UserService userService;
	private final VocService vocService;
	private final ControlService controlService;
	
	public LearnController(VocSetService vocSetService,
	                       UserService userService,
	                       VocService vocService,
	                       ControlService controlService) {
		this.vocSetService = vocSetService;
		this.userService = userService;
		this.vocService = vocService;
		this.controlService = controlService;
	}
	
	@GetMapping("{id}")
	@PreAuthorize("hasAnyAuthority('site:learn')")
	public String create(Principal principal,
	                     Model model,
	                     @PathVariable String id,
	                     @RequestParam(value = "mode", defaultValue = "text") String mode) throws IOException {
		Optional<User> user = controlService.getUser(principal, model, ControlService.MenuID.SET);
		if (user.get(MetaKey.FORCE_PASSWORD_CHANGE)) return "redirect:/myAccount";
		
		WordSet set = vocSetService.findByID(Identifiable.getId(id))
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
