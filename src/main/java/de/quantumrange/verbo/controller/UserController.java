package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.MetaKey;
import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {
	
	private final ControlService controlService;
	private final UserService userService;
	private final InviteService inviteService;
	private final VocDetailService vocDetailService;
	private final PasswordEncoder passwordEncoder;
	private final CommonPasswordDetectionService commonPasswordDetectionService;
	
	@Autowired
	public UserController(ControlService controlService, UserService userService, InviteService inviteService,
	                      VocDetailService vocDetailService, PasswordEncoder passwordEncoder, CommonPasswordDetectionService commonPasswordDetectionService) {
		this.controlService = controlService;
		this.userService = userService;
		this.inviteService = inviteService;
		this.vocDetailService = vocDetailService;
		this.passwordEncoder = passwordEncoder;
		this.commonPasswordDetectionService = commonPasswordDetectionService;
	}
	
	@GetMapping("myAccount")
	@PreAuthorize("hasAnyAuthority('site:my')")
	public String home(Principal principal,
	                   Model model) {
		Optional<User> u = controlService.getUser(principal, model, ControlService.MenuID.USER);
		
		if (u.get(MetaKey.FORCE_PASSWORD_CHANGE)) {
			if (!model.containsAttribute("error")) {
				model.addAttribute("error", "You must change your password after resetting it!");
			}
		}
		
		model.addAttribute("nickname", u.getDisplayName());
		model.addAttribute("amount", vocDetailService.findViewBy(u.getId())
				.map(Map::size)
				.orElse(0));
		
		return "user";
	}
	
	@PostMapping("myAccount/nickname")
	@PreAuthorize("hasAnyAuthority('site:my')")
	public String nickname(Principal principal,
	                       Model model,
	                       @RequestParam("nickname") String nickname) {
		Optional<User> u = controlService.getUser(principal, model, ControlService.MenuID.USER);
		
		nickname = nickname.replaceAll("[^a-zA-Z0-9 _áéíúóñÁÉÍÚÓÑ]", "");
		
		if (nickname.length() >= 30 || nickname.length() <= 3)
			model.addAttribute("error", "Your nickname/display name is too long/short");
		
		if (model.containsAttribute("error"))
			return home(principal, model);
		
		u.setDisplayName(nickname);
		userService.update(u);
		
		model.addAttribute("success", "Your new nickname/display name is set!");
		
		return home(principal, model);
	}
	
	@PostMapping("myAccount/password")
	@PreAuthorize("hasAnyAuthority('site:my')")
	public String nickname(Principal principal,
	                       Model model,
	                       @RequestParam("pw") String pw,
	                       @RequestParam("pwRepeat") String pwRepeat) {
		Optional<User> u = controlService.getUser(principal, model, ControlService.MenuID.USER);
		
		if (!pw.equals(pwRepeat))
			model.addAttribute("error", ".-. Passwords must match");
		
		if (commonPasswordDetectionService.isUsedPassword(pw))
			model.addAttribute("error",
					"This is one of the top 10000 most used passwords. Please educate yourself on how to create a secure password and come back later. (Oh and don't reuse passwords across websites. I'm serious. That's dangerous.)");
		
		if (pw.length() <= 6)
			model.addAttribute("error", "Your password is too short");
		
		if (model.containsAttribute("error"))
			return home(principal, model);
		
		u.setPassword(passwordEncoder.encode(pw));
		u.set(MetaKey.FORCE_PASSWORD_CHANGE, false);
		userService.update(u);
		
		model.addAttribute("success", "Your new password is set!");
		
		return home(principal, model);
	}
	
}
