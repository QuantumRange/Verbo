package de.quantumrange.verbo.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private final PasswordEncoder passwordEncoder;
	private final UserAuthService userService;
	
	@Autowired
	public SecurityConfig(PasswordEncoder passwordEncoder, UserAuthService userService) {
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}
	
	@Override
	protected void configure(@NotNull HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.authorizeRequests()
				.antMatchers("/login", "/register", "/css/**", "/js/**", "/api/user/**").permitAll()
				.anyRequest().authenticated().and()
				.formLogin().loginPage("/login").and()
				.logout().logoutSuccessUrl("/login").and()
				.rememberMe();
	}
	
	@Override
	protected void configure(@NotNull AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(daoAuthenticationProvider());
	}
	
	@Bean
	public @NotNull DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		
		provider.setPasswordEncoder(passwordEncoder);
		provider.setUserDetailsService(userService);
		
		return provider;
	}
}