package de.quantumrange.verbo.service;

import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.model.VocSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class ControlService {

	private final UserService userService;
	private final CourseService courseService;
	private final VocSetService vocSetService;

	@Value("${instantReload}")
	private boolean instantReload;

	@Autowired
	public ControlService(UserService userService, CourseService courseService, VocSetService vocSetService) {
		this.userService = userService;
		this.courseService = courseService;
		this.vocSetService = vocSetService;
	}

	public User getUser(Principal principal,
						Model model,
						MenuID id) {
		User user = userService.findByPrinciple(principal);

		if (instantReload) {
			model.addAttribute("hash", new Random().nextInt());
		} else {
			model.addAttribute("hash", user.getMeta().hashCode());
		}

		model.addAttribute("navbarSelector", id.name);
		model.addAttribute("courses", courseService.stream()
				.filter(c -> c.getUsers().contains(user.getId()))
				.toList());
		model.addAttribute("user", user);

		return user;
	}

	public Stream<VocSet> getSets(User user,
								  Set<Long> set) {
		return set.stream()
				.map(vocSetService::findByID)
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	public enum MenuID {

		HOME("home"),
		COURSES("courses"),
		USERS("users"),
		USER("user"),
		SET("sets"),
		LIVE("live");

		private final String name;

		MenuID(String name) {
			this.name = name;
		}

	}

}
