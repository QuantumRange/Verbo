package de.quantumrange.verbo.service;

import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.model.WordSet;
import de.quantumrange.verbo.service.repos.CourseRepository;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordSetRepository;
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

	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final WordSetRepository wordSetRepository;

	@Value("${instantReload}")
	private boolean instantReload;

	@Autowired
	public ControlService(UserRepository userRepository, CourseRepository courseRepository, WordSetRepository wordSetRepository) {
		this.userRepository = userRepository;
		this.courseRepository = courseRepository;
		this.wordSetRepository = wordSetRepository;
	}

	public Optional<User> getUser(Principal principal,
								  Model model,
								  MenuID id) {
		Optional<User> user = userRepository.findByPrinciple(principal);

		if (user.isEmpty()) return Optional.empty();

		model.addAttribute("hash", instantReload ? new Random().nextInt() : user.get().getMeta().hashCode());
		
		model.addAttribute("navbarSelector", id.name);
		model.addAttribute("courses", courseRepository.findByWatcher(user.get().getId()));
		model.addAttribute("user", user.get());
		
		return user;
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
