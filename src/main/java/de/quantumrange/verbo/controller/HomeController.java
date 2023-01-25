package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.MetaKey;
import de.quantumrange.verbo.model.Role;
import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {
	
	private final UserService userService;
	private final InviteService inviteService;
	private final PasswordEncoder passwordEncoder;
	private final VocSetService vocSetController;
	private final VocService vocController;
	private final CourseService courseController;
	private final ControlService controlService;
	private final PasswordService passwordService;
	
	@Value("${autoLogin}")
	private boolean autoLogin;
	
	@Autowired
	public HomeController(UserService userService, InviteService inviteService, PasswordEncoder passwordEncoder,
	                      VocSetService vocSetController, VocService vocController, CourseService courseController,
	                      ControlService controlService, PasswordService passwordService) {
		this.userService = userService;
		this.inviteService = inviteService;
		this.passwordEncoder = passwordEncoder;
		this.vocSetController = vocSetController;
		this.vocController = vocController;
		this.courseController = courseController;
		this.controlService = controlService;
		this.passwordService = passwordService;
	}
	
	private static String formatting(String str) {
		return str.replaceAll(";", ":").replaceAll("\"", "'");
	}
	
	@GetMapping("sets")
	public String sets(Principal principal,
	                   Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.SET);
		
		if (user.get(MetaKey.FORCE_PASSWORD_CHANGE)) return "redirect:/myAccount";
		
		model.addAttribute("yourSets", vocSetController.parallel()
				.filter(v -> v.getOwner() == user.getId())
				.collect(Collectors.toSet()));
		model.addAttribute("markedSets", vocSetController.parallel()
				.filter(v -> user.getMarked().contains(v.getId()))
				.collect(Collectors.toSet()));
		model.addAttribute("sets", vocSetController.parallel()
				.collect(Collectors.toSet()));
		
		return "sets";
	}
	
	@GetMapping("live")
	public String live(Principal principal,
	                   Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.LIVE);
		
		return "live/student";
	}
	
	@GetMapping
	@PreAuthorize("hasAnyAuthority('site:home')")
	public String home(Principal principal,
	                   Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.HOME);
		
		if (user.get(MetaKey.FORCE_PASSWORD_CHANGE)) return "redirect:/myAccount";
		
		return "home";
	}
	
	@GetMapping("login")
	public @NotNull String login(Model model,
	                             @RequestParam(name = "username", defaultValue = "") String username) {
		model.addAttribute("username", autoLogin ? "QuantumRange" : username);
		model.addAttribute("password", autoLogin ? "password" : "");
		return "login";
	}
	
	@GetMapping("register")
	public @NotNull String register() {
		return "register";
	}
	
	
	@PostMapping("register")
	@ResponseBody
	public String register(@NotNull Model m,
	                       @RequestBody RegisterRequest request) {
		String username = request.username
				.trim()
				.replaceAll("[^A-Za-zàèìòùÀÈÌÒÙ_\\-()?¿! ]", "");
		
		username = username.isBlank() ? null : username;
		
		String nickname = request.nickname
				.trim()
				.replaceAll("[^A-Za-zàèìòùÀÈÌÒÙ_\\-()?¿! ]", "");
		nickname = nickname.isBlank() ? null : nickname;
		
		String pw = request.password.isBlank() ? null : request.password;
		String pwRepeat = request.repeatPassword.isBlank() ? null : request.repeatPassword;
		String inviteToken = request.inviteCode.isBlank() ? null : request.inviteCode;
		
		if (username == null ||
				nickname == null ||
				pw == null ||
				pwRepeat == null ||
				inviteToken == null) {
			List<String> elements = new ArrayList<>();
			
			if (username == null) elements.add("Username");
			if (nickname == null) elements.add("Nickname");
			if (pw == null) elements.add("Password");
			if (pwRepeat == null) elements.add("Repeat Password");
			if (inviteToken == null) elements.add("Invite Code");
			
			return "Following fields are empty or invalid: " + String.join(", ", elements);
		}
		
		if (!pw.equals(pwRepeat))
			return ".-. Passwords must match";
		
		if (passwordService.isUsedPassword(pw))
			return "This is one of the top 10000 most used passwords. Please educate yourself on how to create a secure password and come back later. (Oh and don't reuse passwords across websites. I'm serious. That's dangerous.)";
		
		if (pw.length() <= 6)
			return "Your password is too short";
		
		if (!inviteService.exist(inviteToken))
			return "Can't find that invite code.";
		
		if (userService.findByUsername(username).isPresent())
			return "Username exists already!";
		
		User user = new User(userService.generateID(),
				username,
				nickname,
				passwordEncoder.encode(pw),
				Role.USER);
		
		userService.insert(user);
		
		return "success:" + user.getUsername();
	}
	
	record RegisterRequest(String username,
	                       String nickname,
	                       String password,
	                       String repeatPassword,
	                       String inviteCode) {
	}
	
}
