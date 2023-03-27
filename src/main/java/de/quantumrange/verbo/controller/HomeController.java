package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.Invite;
import de.quantumrange.verbo.model.MetaKey;
import de.quantumrange.verbo.model.Role;
import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.CommonPasswordDetectionService;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.repos.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
public class HomeController {
	
	private final UserRepository userRepository;
	private final InviteRepository inviteRepository;
	private final PasswordEncoder passwordEncoder;
	private final WordSetRepository wordSetRepository;
	private final WordRepository wordRepository;
	private final CourseRepository courseRepository;
	private final ControlService controlService;
	private final CommonPasswordDetectionService cpdService;
	
	@Value("${autoLogin}")
	private boolean autoLogin;
	
	@Autowired
	public HomeController(UserRepository userRepository,
	                      InviteRepository inviteRepository,
	                      PasswordEncoder passwordEncoder,
	                      WordSetRepository wordSetRepository,
	                      WordRepository wordRepository,
	                      CourseRepository courseRepository,
	                      ControlService controlService, CommonPasswordDetectionService cpdService) {
		this.userRepository = userRepository;
		this.inviteRepository = inviteRepository;
		this.passwordEncoder = passwordEncoder;
		this.wordSetRepository = wordSetRepository;
		this.wordRepository = wordRepository;
		this.courseRepository = courseRepository;
		this.controlService = controlService;
		this.cpdService = cpdService;
	}

//	private static String formatting(String str) {
//		return str.replaceAll(";", ":").replaceAll("\"", "'");
//	}
	
	@GetMapping("sets")
	public String sets(Principal principal,
	                   Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.SET)
				.orElseThrow();
		
		if (user.isForcedPasswordChange()) return "redirect:/myAccount";
		
		model.addAttribute("yourSets", wordSetRepository.findWordSetsByOwner(user.getId()));
		model.addAttribute("markedSets", user.getMarked());
		
		return "sets";
	}
	
	@GetMapping("live")
	public String live(Principal principal,
	                   Model model) {
		// TODO
		User user = controlService.getUser(principal, model, ControlService.MenuID.LIVE)
				.orElseThrow();
		
		return "live/student";
	}
	
	@GetMapping
	@PreAuthorize("hasAnyAuthority('site:home')")
	public String home(Principal principal,
	                   Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.HOME)
				.orElseThrow();
		
		if (user.isForcedPasswordChange()) return "redirect:/myAccount";
		
		return "home";
	}
	
	@GetMapping("login")
	public @NotNull String login(Model model,
	                             @RequestParam(name = "username", defaultValue = "") String username) {
		model.addAttribute("username", autoLogin ? "root" : username);
		model.addAttribute("password", autoLogin ? "root" : "");
		return "login";
	}
	
	@GetMapping("register")
	public @NotNull String register() {
		return "register";
	}
	
	
	@PostMapping("register")
	@ResponseBody
	public String register(@RequestBody RegisterRequest request) {
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
		
		if (cpdService.isUsedPassword(pw))
			return "This is one of the top 10000 most used passwords. Please educate yourself on how to create a secure password and come back later. (Oh and don't reuse passwords across websites. I'model serious. That's dangerous.)";
		
		if (pw.length() <= 6)
			return "Your password is too short";
		
		Optional<Invite> code = inviteRepository.findByCode(inviteToken);
		
		if (code.isEmpty())
			return "Can't find that invite code.";
		
		Invite invite = code.get();
		
		if (!invite.isValid())
			return "Invite isn't valid anymore.";
		
		if (userRepository.existsUsername(username))
			return "Username exists already!";
		
		User user = new User(0L,
				username,
				nickname,
				passwordEncoder.encode(pw),
				new ArrayList<>(),
				new HashSet<>(),
				new HashMap<>(),
				Role.USER);
		
		userRepository.saveAndFlush(user);
		
		return "success:" + user.getUsername();
	}
	
	record RegisterRequest(String username,
	                       String nickname,
	                       String password,
	                       String repeatPassword,
	                       String inviteCode) {
	}
	
}
