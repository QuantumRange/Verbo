package de.quantumrange.verbo.controller;

import de.quantumrange.verbo.model.ColorTheme;
import de.quantumrange.verbo.model.MetaKey;
import de.quantumrange.verbo.model.User;
import de.quantumrange.verbo.service.ControlService;
import de.quantumrange.verbo.service.UserService;
import de.quantumrange.verbo.service.VocSetService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/user")
public class UserApiController {

	private final VocSetService service;
	private final UserService userService;
	private final ControlService controlService;

	public UserApiController(VocSetService service, UserService userService, ControlService controlService) {
		this.service = service;
		this.userService = userService;
		this.controlService = controlService;
	}

	@PostMapping(path = "request")
	@PreAuthorize("hasAnyAuthority('api:user:request')")
	public Map<String, Object> client(Principal principal) {
		User user = userService.findByPrinciple(principal);

		final Map<String, Object> data = new HashMap<>();

		data.put(MetaKey.COLOR_THEME.getName(), ColorTheme.valueOfElse(user.get(MetaKey.COLOR_THEME)).getVariables());
		data.put(MetaKey.LEARNING_LOOP_AMOUNT.getName(), user.get(MetaKey.LEARNING_LOOP_AMOUNT));
		data.put(MetaKey.LEARNING_SETTINGS.getName(), user.get(MetaKey.LEARNING_SETTINGS));


		return data;
	}

	@PostMapping(path = "update")
	@PreAuthorize("hasAnyAuthority('api:user:update')")
	public boolean update(Principal principal,
						  @RequestBody Map<String, String> map) {
		User user = userService.findByPrinciple(principal);

		String key = map.get("key");
		String value = map.get("value");

		MetaKey<?> metaKey = null;

		for (MetaKey<?> val : MetaKey.values) {
			if (val.getName().equals(key)) {
				metaKey = val;
				break;
			}
		}

		if (metaKey == null) return false;

		metaKey.set(user, value);

		userService.update(user);

		return true;
	}

}
