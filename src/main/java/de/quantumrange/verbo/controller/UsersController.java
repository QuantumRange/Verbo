package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.Identifiable;
import de.quantumrange.verbo.model.MetaKey;
import de.quantumrange.verbo.model.Role;
import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.InviteService;
import de.quantumrange.verbo.service.UserService;
import de.quantumrange.verbo.service.VocDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class UsersController {
	
	private final ControlService controlService;
	private final UserService userService;
	private final InviteService inviteService;
	private final VocDetailService vocDetailService;
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
	public UsersController(ControlService controlService, UserService userService, InviteService inviteService, VocDetailService vocDetailService, PasswordEncoder passwordEncoder) {
		this.controlService = controlService;
		this.userService = userService;
		this.inviteService = inviteService;
		this.vocDetailService = vocDetailService;
		this.passwordEncoder = passwordEncoder;
	}
	
	@GetMapping("users")
	@PreAuthorize("hasAnyAuthority('site:users')")
	public String home(Principal principal,
	                   Model model) {
		User u = controlService.getUser(principal, model, ControlService.MenuID.USERS);
		
		model.addAttribute("users", userService.stream()
				.sorted((o1, o2) -> {
					int com = Integer.compare(o1.getRole().ordinal(), o2.getRole().ordinal());
					
					if (com == 0) {
						return CharSequence.compare(o1.getUsername(), o2.getUsername());
					} else return com;
				})
				.toList());
		
		Map<Long, Integer> map = new HashMap<>();
		
		vocDetailService.getData().forEach((id, m) -> map.put(id, m.size()));
		
		model.addAttribute("roles", Role.values());
		model.addAttribute("map", map);
		
		return "users";
	}
	
	@GetMapping("users/invite")
	@PreAuthorize("hasAnyAuthority('generate:invite')")
	public String invite(Principal principal,
	                     Model model) {
		model.addAttribute("inviteCode", inviteService.generateCode());
		
		return home(principal, model);
	}
	
	@PostMapping("users/{user}/role")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	@ResponseBody
	public boolean updateRole(Principal principal,
	                          Model model,
	                          @PathVariable String user,
	                          @RequestBody String role) {
		User c = userService.findByPrinciple(principal);
		User u = userService.findByID(Identifiable.getId(user))
				.orElseThrow();
		Role r = Role.valueOf(role);
		
		if (u.getRole().ordinal() >= c.getRole().ordinal() &&
				r.ordinal() >= c.getRole().ordinal()) {
			u.setRole(r);
			userService.update(u);
			return true;
		}
		
		return false;
	}
	
	@GetMapping("users/{user}/reset")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	public String resetPassword(Principal principal,
	                            @PathVariable String user,
	                            Model model) {
		
		User u = controlService.getUser(principal, model, ControlService.MenuID.USERS);
		User other = userService.findByID(Identifiable.getId(user))
				.orElseThrow();
		
		if (other.getRole().ordinal() <= u.getRole().ordinal()) {
			return "redirect:/users";
		}
		
		model.addAttribute("username", other.getUsername());
		model.addAttribute("otherID", other.getVisibleId());
		
		return "resetPassword";
	}
	
	
	@GetMapping("users/{user}/delete")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	public String deleteUser(Principal principal,
	                         @PathVariable String user,
	                         Model model) {
		
		User u = controlService.getUser(principal, model, ControlService.MenuID.USERS);
		User other = userService.findByID(Identifiable.getId(user))
				.orElseThrow();
		
		if (other.getRole().ordinal() <= u.getRole().ordinal()) {
			return "redirect:/users";
		}
		
		model.addAttribute("username", other.getUsername());
		model.addAttribute("otherID", other.getVisibleId());
		
		return "resetUser";
	}
	
	@GetMapping("users/{user}/confirm")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	public String resetPasswordConfirm(Principal principal,
	                                   Model model,
	                                   @PathVariable String user) {
		User c = controlService.getUser(principal, model, ControlService.MenuID.USERS);
		User other = userService.findByID(Identifiable.getId(user))
				.orElseThrow();
		
		if (other.getRole().ordinal() <= c.getRole().ordinal()) return "redirect:/";
		
		final char[] charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		final Random rnd = new Random();
		final StringBuilder pw = new StringBuilder();
		
		for (int i = 0; i < 20; i++) {
			pw.append(charset[rnd.nextInt(charset.length)]);
		}
		
		model.addAttribute("newPassword", pw.toString());
		model.addAttribute("username", other.getUsername());
		
		other.set(MetaKey.FORCE_PASSWORD_CHANGE, true);
		other.setPassword(passwordEncoder.encode(pw.toString()));
		
		userService.update(other);
		return "resetPasswordSuccess";
	}
	
	@GetMapping("users/{user}/delete/confirm")
	@PreAuthorize("hasAnyAuthority('api:delete:user')")
	public String resetPassword(Principal principal,
	                            Model model,
	                            @PathVariable String user) {
		User c = controlService.getUser(principal, model, ControlService.MenuID.USERS);
		User other = userService.findByID(Identifiable.getId(user))
				.orElseThrow();
		
		if (c.getRole() != Role.ROOT || other.getRole() == Role.ROOT) return "redirect:/";
		
		userService.remove(other);
		return "redirect:/users";
	}
	
}
