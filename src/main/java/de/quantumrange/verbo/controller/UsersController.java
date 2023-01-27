package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.Identifiable;
import de.quantumrange.verbo.model.Invite;
import de.quantumrange.verbo.model.Role;
import de.quantumrange.verbo.model.User;
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

		Invite invite = inviteRepository.saveAndFlush(new Invite(0L, Invite.generateCode(), user, LocalDateTime.now(), new ArrayList<>(), true));

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
				newRole.ordinal() < user.getRole().ordinal()) {
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


	@GetMapping("users/{user}/delete")
	@PreAuthorize("hasAnyAuthority('api:update:role')")
	public String deleteUser(Principal principal,
							 @PathVariable String user,
							 Model model) {

		Optional<User> u = controlService.getUser(principal, model, ControlService.MenuID.USERS);
		User other = userRepository.findByID(Identifiable.getId(user))
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
		Optional<User> c = controlService.getUser(principal, model, ControlService.MenuID.USERS);
		User other = userRepository.findByID(Identifiable.getId(user))
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

		userRepository.update(other);
		return "resetPasswordSuccess";
	}

	@GetMapping("users/{user}/delete/confirm")
	@PreAuthorize("hasAnyAuthority('api:delete:user')")
	public String resetUserPasswordByName(Principal principal,
										  Model model,
										  @PathVariable String user) {
		Optional<User> c = controlService.getUser(principal, model, ControlService.MenuID.USERS);
		User other = userRepository.findByID(Identifiable.getId(user))
				.orElseThrow();

		if (c.getRole() != Role.ROOT || other.getRole() == Role.ROOT) return "redirect:/";

		userRepository.remove(other);
		return "redirect:/users";
	}

}
