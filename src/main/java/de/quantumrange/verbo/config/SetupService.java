package de.quantumrange.verbo.config;

import de.quantumrange.verbo.model.MetaKey;
import de.quantumrange.verbo.model.Role;
import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.repos.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Service
@Component
public class SetupService implements CommandLineRunner {
	
	private static final Logger log = LoggerFactory.getLogger(SetupService.class);
 
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
	public SetupService(UserRepository userRepository,
	                    PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	public void run(String... args) {
		if (!userRepository.existsAny()) {
			log.warn("No users found! Creating admin user...");
			
			User user = userRepository.saveAndFlush(new User(
					0L,
					"root",
					"ROOT",
					passwordEncoder.encode("root"),
					new ArrayList<>(),
					new HashSet<>(),
					new HashMap<>(),
					Role.ADMIN
			));
			
			user.set(MetaKey.FORCE_PASSWORD_CHANGE, true);
			userRepository.saveAndFlush(user);
			
			log.info("Root user {} :: username: root, password: root", user);
		}
	}
}
