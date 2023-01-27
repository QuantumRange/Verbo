package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.*;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.repos.InviteRepository;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

@Controller
public class UsersController {

	private static final Logger log = LoggerFactory.getLogger(UsersController.class);

	private final ControlService controlService;
	private final UserRepository userRepository;
	private final InviteRepository inviteRepository;
	private final WordSetRepository wordSetRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public UsersController(ControlService controlService,
						   UserRepository userRepository,
						   InviteRepository inviteRepository,
						   WordSetRepository wordSetRepository,
						   PasswordEncoder passwordEncoder) {
		this.controlService = controlService;
		this.userRepository = userRepository;
		this.inviteRepository = inviteRepository;
		this.wordSetRepository = wordSetRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("users")
	@PreAuthorize("hasAnyAuthority('site:users')")
	public String users(Principal principal,
						Model model) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.USERS)
				.orElseThrow();

		model.addAttribute("users", userRepository.findByAllOrderByRoleAscUsernameAsc());

		model.addAttribute("roles", Role.values());

		return "users";
	}

	@GetMapping("users/invite")
	@PreAuthorize("hasAnyAuthority('generate:invite')")
	public String generateInvite(Principal principal,
								 Model model) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();

		Invite invite = inviteRepository.saveAndFlush(new Invite(0L, Invite.generateCode(), user, LocalDateTime.now(), true));

		model.addAttribute("inviteCode", invite.getCode());

		return users(principal, model);
	}

	@PostMapping("users/{user}/role")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	@ResponseBody
	public boolean updateUserRole(Principal principal,
								  @PathVariable(name = "user") String userIdStr,
								  @RequestBody String role) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		User targetUser = userRepository.findById(Identifiable.getId(userIdStr))
				.orElseThrow();
		Role newRole = Role.valueOf(role);

		if (targetUser.getRole().ordinal() < user.getRole().ordinal() ||
				newRole.ordinal() <= user.getRole().ordinal()) {
			log.warn("{} tried to change {} user's role from {} to {} but don't has the permission!", user, targetUser, role, newRole);
			return false;
		}

		log.warn("{} modified {} user's role from {} to {}", user, targetUser, role, newRole);
		targetUser.setRole(newRole);
		userRepository.saveAndFlush(targetUser);

		return true;

	}

	@GetMapping("users/{user}/reset")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	public String resetUserPasswordByName(Principal principal,
										  @PathVariable(name = "user") String userIdStr,
										  Model model) {
		// TODO: Replace with ConfirmController
		User user = controlService.getUser(principal, model, ControlService.MenuID.USERS)
				.orElseThrow();
		User other = userRepository.findById(Identifiable.getId(userIdStr))
				.orElseThrow();

		if (other.getRole().ordinal() <= user.getRole().ordinal()) {
			return "redirect:/users";
		}

		model.addAttribute("username", other.getUsername());
		model.addAttribute("otherID", other.getVisibleId());

		return "resetPassword";
	}


	@GetMapping("users/{userIdStr}/delete")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	public String deleteUser(Principal principal,
							 @PathVariable String userIdStr,
							 Model model) {
		// TODO: Replace with ConfirmController
		User user = controlService.getUser(principal, model, ControlService.MenuID.USERS)
				.orElseThrow();
		User target = userRepository.findById(Identifiable.getId(userIdStr))
				.orElseThrow();

		if (target.getRole().ordinal() <= user.getRole().ordinal()) {
			return "redirect:/users";
		}

		model.addAttribute("username", target.getUsername());
		model.addAttribute("otherID", target.getVisibleId());

		return "resetUser";
	}

	@GetMapping("users/{userIdStr}/confirm")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	public String resetPasswordConfirm(Principal principal,
									   Model model,
									   @PathVariable String userIdStr) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.USERS)
				.orElseThrow();
		User target = userRepository.findById(Identifiable.getId(userIdStr))
				.orElseThrow();
		
		if (target.getRole().ordinal() <= user.getRole().ordinal()) {
			log.warn("{} tried to reset {} user's password but don't has the permission!", userIdStr, target);
			return "redirect:/";
		}

		final char[] charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		final Random rnd = new Random();
		final StringBuilder pw = new StringBuilder();

		for (int i = 0; i < 10; i++) {
			pw.append(charset[rnd.nextInt(charset.length)]);
		}

		model.addAttribute("newPassword", pw.toString());
		model.addAttribute("username", target.getUsername());

		target.getMeta().put(MetaKey.FORCE_PASSWORD_CHANGE.getMapKey(), String.valueOf(true));
		target.setPassword(passwordEncoder.encode(pw.toString()));
		
		userRepository.saveAndFlush(target);
		
		return "resetPasswordSuccess";
	}

	@GetMapping("users/{userIdStr}/delete/confirm")
	@PreAuthorize("hasAnyAuthority('api:delete:user')")
	public String resetUserPasswordByName(Principal principal,
										  Model model,
										  @PathVariable String userIdStr) {
		User user = controlService.getUser(principal, model, ControlService.MenuID.USERS)
				.orElseThrow();
		User target = userRepository.findById(Identifiable.getId(userIdStr))
				.orElseThrow();

		if (user.getRole() != Role.ADMIN || target.getRole() == Role.ADMIN) return "redirect:/";

		userRepository.delete(target);
		
		return "redirect:/users";
	}

}
