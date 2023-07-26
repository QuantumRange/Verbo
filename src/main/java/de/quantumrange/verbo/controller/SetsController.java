package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.repos.WordSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("sets")
public class SetsController {
	private static final Logger log = LoggerFactory.getLogger(SetsController.class);
	
	private final ControlService controlService;
	private final WordSetRepository wordSetRepository;
	
	@Autowired
	public SetsController(ControlService controlService, WordSetRepository wordSetRepository) {
		this.controlService = controlService;
		this.wordSetRepository = wordSetRepository;
	}
	
	@GetMapping(path = "")
	public String viewSets(Principal principal,
	                   Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.SET)
				.orElseThrow();
		
		if (user.isForcedPasswordChange()) return "redirect:/myAccount";
		
		model.addAttribute("yourSets", wordSetRepository.findWordSetsByOwner(user.getId()));
		model.addAttribute("markedSets", user.getMarked());
		
		return "sets";
	}
	
	@GetMapping(path = "new")
	public String newSets(Principal principal,
	                   Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.SET)
				.orElseThrow();
		
		
		
		return "newSet";
	}
	
}
