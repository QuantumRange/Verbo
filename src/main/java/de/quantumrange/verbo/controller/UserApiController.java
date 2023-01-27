package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.MetaKey;
import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.repos.UserRepository;
import de.quantumrange.verbo.service.repos.WordSetRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/user")
public class UserApiController {
	
	private final WordSetRepository wordSetRepository;
	private final UserRepository userRepository;
	private final ControlService controlService;
	
	public UserApiController(WordSetRepository wordSetRepository,
	                         UserRepository userRepository,
	                         ControlService controlService) {
		this.wordSetRepository = wordSetRepository;
		this.userRepository = userRepository;
		this.controlService = controlService;
	}
	
	@PostMapping(path = "request")
	@PreAuthorize("hasAnyAuthority('api:user:request')")
	public Map<String, Object> client(Principal principal) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		
		final Map<String, Object> data = new HashMap<>();
		
		data.put(MetaKey.LEARNING_LOOP_AMOUNT.getMapKey(), user.get(MetaKey.LEARNING_LOOP_AMOUNT, null));
		data.put(MetaKey.LEARNING_SETTINGS.getMapKey(), user.get(MetaKey.LEARNING_SETTINGS, null));
		
		return data;
	}
	
	@PostMapping(path = "update")
	@PreAuthorize("hasAnyAuthority('api:user:update')")
	public boolean update(Principal principal,
	                      @RequestBody Map<String, String> map) {
		User user = userRepository.findByPrinciple(principal)
				.orElseThrow();
		
		String key = map.get("key");
		String value = map.get("value");
		
		boolean exists = Arrays.stream(MetaKey.values())
				.anyMatch(mk -> mk.getMapKey().equals(key));
		
		if (!exists) return false;
		
		user.getMeta().put(key, value);
		
		userRepository.save(user);
		
		return true;
	}
	
}
