package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.MetaKey;
import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.CommonPasswordDetectionService;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.repos.InviteRepository;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class UserController {
	
	private final ControlService controlService;
	private final UserRepository userRepository;
	private final InviteRepository inviteRepository;
	private final WordViewRepository wordViewRepository;
	private final PasswordEncoder passwordEncoder;
	private final CommonPasswordDetectionService cpdService;
	
	@Autowired
	public UserController(ControlService controlService,
	                      UserRepository userRepository,
	                      InviteRepository inviteRepository,
	                      WordViewRepository wordViewRepository,
	                      PasswordEncoder passwordEncoder,
	                      CommonPasswordDetectionService cpdService) {
		this.controlService = controlService;
		this.userRepository = userRepository;
		this.inviteRepository = inviteRepository;
		this.wordViewRepository = wordViewRepository;
		this.passwordEncoder = passwordEncoder;
		this.cpdService = cpdService;
	}
	
	@GetMapping("myAccount")
	@PreAuthorize("hasAnyAuthority('site:my')")
	public String myAccount(Principal principal,
	                        Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.USER)
				.orElseThrow();
		
		if (user.isForcedPasswordChange()) {
			// If password error is in model then don't show that
			if (!model.containsAttribute("error")) {
				model.addAttribute("error", "You must change your password after resetting it!");
			}
		}
		
		model.addAttribute("nickname", user.getDisplayName());
		model.addAttribute("amount", wordViewRepository.countByUserId(user.getId()));
		
		return "user";
	}
	
	@PostMapping("myAccount/nickname")
	@PreAuthorize("hasAnyAuthority('site:my')")
	public String nickname(Principal principal,
	                       Model model,
	                       @RequestParam("nickname") String nickname) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.USER)
				.orElseThrow();
		
		nickname = nickname.replaceAll("[^a-zA-Z0-9 _áéíúóñÁÉÍÚÓÑ]", "");
		
		if (nickname.length() >= 30 || nickname.length() <= 3) {
			model.addAttribute("error", "Your nickname/display name is too long/short");
		}
		
		if (model.containsAttribute("error")) {
			return myAccount(principal, model);
		}
		
		user.setDisplayName(nickname);
		userRepository.saveAndFlush(user);
		
		model.addAttribute("success", "Your new nickname/display name is set!");
		
		return myAccount(principal, model);
	}
	
	@PostMapping("myAccount/password")
	@PreAuthorize("hasAnyAuthority('site:my')")
	public String password(Principal principal,
	                       Model model,
	                       @RequestParam("pw") String pw,
	                       @RequestParam("pwRepeat") String pwRepeat) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.USER)
				.orElseThrow();
		
		if (!pw.equals(pwRepeat))
			model.addAttribute("error", ".-. Passwords must match");
		
		if (cpdService.isUsedPassword(pw)) {
			model.addAttribute("error", "This is one of the top 10000 most used passwords. Please educate yourself on how to create a secure password and come back later. (Oh and don't reuse passwords across websites. I'm serious. That's dangerous.)");
		}
		
		if (pw.length() <= 6) {
			model.addAttribute("error", "Your password is too short");
		}
		
		if (model.containsAttribute("error")) {
			return myAccount(principal, model);
		}
		
		user.setPassword(passwordEncoder.encode(pw));
		user.set(MetaKey.FORCE_PASSWORD_CHANGE, false);
		userRepository.saveAndFlush(user);
		
		model.addAttribute("success", "Your new password is set!");
		
		return myAccount(principal, model);
	}
	
}
