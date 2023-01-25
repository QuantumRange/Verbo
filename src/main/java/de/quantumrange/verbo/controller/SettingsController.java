package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.ControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("setting")
public class SettingsController {

	private final ControlService controlService;

	@Autowired
	public SettingsController(ControlService controlService) {
		this.controlService = controlService;
	}

	@GetMapping("/learn")
	public String learn(Principal principal, Model model) {
		User u = controlService.getUser(principal, model, ControlService.MenuID.USER);

		return "setting/learn";
	}

}
