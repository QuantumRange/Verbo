package de.quantumrange.verbo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User implements UserDetails, Identifiable {

	@Serial
	private static final long serialVersionUID = -8234328935424819142L;
	private static final Logger log = LoggerFactory.getLogger(User.class);

	private long id;
	private String username;
	private String displayName;
	private String password;
	private Set<Long> marked;
	private MetaData meta;
	private Role role;

	public User(long id,
				String username,
				String displayName,
				String password,
				Role role) {
		this.id = id;
		this.username = username;
		this.displayName = displayName;
		this.password = password;
		this.marked = new HashSet<>();
		this.meta = new MetaData();
		this.role = role;
	}

	@JsonIgnore
	public <T> T get(MetaKey<T> key) {
		if (meta.containsKey(key)) {
			try {
				return (T) meta.get(key);
			} catch (Exception e) {
				log.warn("Can't cast value {} with key {}.", meta.get(key), key.getName());
				return key.getDefaultValue();
			}
		}
		return key.getDefaultValue();
	}

	@JsonIgnore
	public <T> void set(MetaKey<T> key, T value) {
		meta.put(key, value);
	}

	public boolean hasSet(VocSet set) {
		return marked.contains(set.getId());
	}

	public boolean hasPermission(Permission... permissions) {
		for (Permission permission : permissions) {
			if (!role.getPermissions().contains(permission)) {
				return false;
			}
		}

		return true;
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isEnabled() {
		return true;
	}

	@JsonIgnore
	@Override
	public @NotNull Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>(role.getPermissions());
		grantedAuthorities.add(role.getGrantedAuthority());

		return grantedAuthorities;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return getId() == user.getId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

}
