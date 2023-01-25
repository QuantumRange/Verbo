package de.quantumrange.verbo.model;

import org.springframework.security.core.GrantedAuthority;

public enum Permission implements GrantedAuthority {
	
	API_EDIT("api:user:update"),
	API_REQUEST("api:user:request"),
	API_MARK("api:set:mark"),
	API_UPDATE_USER_ROLE("api:update:role"),
	API_DELETE_USER("api:delete:user"),
	API_BASIS_REQUESTS("api:basic"),
	
	/* ================================== */
	USER_LOGIN("user:login"),
	GENERATE_INVITE("generate:invite"),
	
	/* ================================== */
	
	COURSE_JOIN("course:join"),
	COURSE_CREATE("course:create"),
	COURSE_VIEW("course:view"),
	COURSE_VIEW_INVITE_CODE("course:view:invite_code"),
	COURSE_EDIT("course:edit"),
	COURSE_VIEW_ALL("course:view:all"),
	
	/* ================================== */
	
	SITE_HOME("site:home"),
	SITE_MY("site:my"),
	SITE_SETS("site:sets"),
	SITE_LEARN("site:learn"),
	SITE_SET("site:set"),
	SITE_IMPORT("site:import"),
	SITE_COURSE("site:course"),
	SITE_COURSES("site:courses"),
	SITE_LIVE("site:live"),
	SITE_USERS("site:users");
	
	private final String permission;
	
	Permission(String permission) {
		this.permission = permission;
	}
	
	@Override
	public String getAuthority() {
		return permission;
	}
}
