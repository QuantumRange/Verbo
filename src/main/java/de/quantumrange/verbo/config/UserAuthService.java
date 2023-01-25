package de.quantumrange.verbo.config;

import de.quantumrange.verbo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class UserAuthService implements UserDetailsService {

	private final UserService userService;

	@Autowired
	public UserAuthService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userService.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("The Username %s not found.".formatted(username)));
	}

}
