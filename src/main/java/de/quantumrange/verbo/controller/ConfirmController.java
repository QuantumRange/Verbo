package de.quantumrange.verbo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * /api/confirm
 * "sourceUrl" is the source url
 * "targetUrl" is the target url for the user after approval
 * "confirmMessage" is the message for the user
 */
@Controller
@RequestMapping("api")
public class ConfirmController {
	
	@GetMapping("/confirm")
	public String confirm(Model model) {
//		if (!model.containsAttribute("sourceUrl")) return "redirect:/";
//		if (!model.containsAttribute("targetUrl")) return "redirect:/";
//		if (!model.containsAttribute("confirmMessage")) return "redirect:/";
		
		model.addAttribute("sourceUrl", "");
		model.addAttribute("targetUrl", "");
		model.addAttribute("confirmMessage", "");
		
		// TODO:
		
		return "confirm";
	}

}
