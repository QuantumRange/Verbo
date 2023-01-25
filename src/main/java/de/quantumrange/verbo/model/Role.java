package de.quantumrange.verbo.model;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.EnumSet;
import java.util.Set;

import static de.quantumrange.verbo.model.Permission.*;

public enum Role {

	ROOT("Root", "fa-solid fa-user-secret", EnumSet.allOf(Permission.class)),
	ADMIN("Admin", "fa-solid fa-shield", EnumSet.allOf(Permission.class)),
	TEACHER("Teacher", "fa-solid fa-chalkboard-user", Sets.newHashSet(
			USER_LOGIN,
			API_EDIT,
			API_REQUEST,
			API_MARK,
			API_BASIS_REQUESTS,
			API_UPDATE_USER_ROLE,
			GENERATE_INVITE,
			COURSE_CREATE,
			COURSE_VIEW_INVITE_CODE,
			COURSE_VIEW_ALL,
			COURSE_EDIT,
			SITE_USERS,
			COURSE_JOIN,
			COURSE_VIEW,
			SITE_HOME,
			SITE_SETS,
			SITE_SET,
			SITE_LEARN,
			SITE_COURSE,
			SITE_COURSES,
			SITE_LIVE,
			SITE_MY
	)),
	USER("Student", "fa-solid fa-user-graduate", Sets.newHashSet(
			USER_LOGIN,
			API_EDIT,
			API_REQUEST,
			API_MARK,
			API_BASIS_REQUESTS,
			COURSE_JOIN,
			COURSE_VIEW,
			SITE_HOME,
			SITE_SETS,
			SITE_SET,
			SITE_LEARN,
			SITE_COURSE,
			SITE_COURSES,
			// SITE_LIVE, TODO: uncomment if live is done!
			SITE_MY));

	private final @NotNull String displayName;
	private final @NotNull String style;
	private final @NotNull Set<Permission> permissions;

	Role(@NotNull String displayName, @NotNull String style, @NotNull Set<Permission> permissions) {
		this.displayName = displayName;
		this.style = style;
		this.permissions = permissions;
	}

	public static @Nullable Role getUserByGrantedAuthority(@NotNull GrantedAuthority authority) {
		for (Role role : values()) {
			if (authority.getAuthority().equals("ROLE_" + role.name())) return role;
		}

		return null;
	}

	public @NotNull String getStyle() {
		return style;
	}

	public @NotNull String getDisplayName() {
		return displayName;
	}

	public @NotNull Set<Permission> getPermissions() {
		return permissions;
	}

	public @NotNull SimpleGrantedAuthority getGrantedAuthority() {
		return new SimpleGrantedAuthority("ROLE_" + this.name());
	}

}
