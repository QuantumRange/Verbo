package de.quantumrange.verbo.model;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.EnumSet;
import java.util.Set;

import static de.quantumrange.verbo.model.Permission.*;

@Getter
@AllArgsConstructor

public enum Role {
	
	ADMIN("Admin", "fa-solid fa-user-secret", EnumSet.allOf(Permission.class)),
	TEACHER("Teacher", "fa-solid fa-chalkboard-user", Sets.newHashSet(
			USER_LOGIN,
			API_EDIT, API_REQUEST, API_MARK, API_BASIS_REQUESTS, API_UPDATE_USER_ROLE,
			GENERATE_INVITE,
			COURSE_CREATE, COURSE_VIEW_INVITE_CODE, COURSE_VIEW_ALL, COURSE_EDIT, COURSE_JOIN, COURSE_VIEW,
			SITE_HOME, SITE_SETS, SITE_SET, SITE_LEARN, SITE_COURSE, SITE_COURSES, SITE_LIVE, SITE_MY, SITE_USERS
	)),
	USER("Student", "fa-solid fa-user-graduate", Sets.newHashSet(
			USER_LOGIN,
			API_EDIT, API_REQUEST, API_MARK, API_BASIS_REQUESTS,
			COURSE_JOIN, COURSE_VIEW,
			SITE_HOME, SITE_SETS, SITE_SET, SITE_LEARN, SITE_COURSE, SITE_COURSES,
			// SITE_LIVE, TODO: uncomment if live is done!
			SITE_MY));
	
	private final @NotNull String displayName;
	private final @NotNull String style;
	private final @NotNull Set<Permission> permissions;
	
	public static @Nullable Role getUserByGrantedAuthority(@NotNull GrantedAuthority authority) {
		for (Role role : values()) {
			if (authority.getAuthority().equals("ROLE_" + role.name())) return role;
		}
		
		return null;
	}
	
	public @NotNull SimpleGrantedAuthority getGrantedAuthority() {
		return new SimpleGrantedAuthority("ROLE_" + this.name());
	}
	
}
