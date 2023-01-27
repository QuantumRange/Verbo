package de.quantumrange.verbo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@Table(appliesTo = "user")
@Entity(name = "user")
public class User implements UserDetails, Identifiable {
	
	private static final Logger log = LoggerFactory.getLogger(User.class);
	
	@Id
	@GenericGenerator(name = "id",
			strategy = "de.quantumrange.verbo.model.generator.IdGenerator",
			parameters = {@Parameter(name = "table", value = "user")})
	@GeneratedValue(generator = "id")
	private long id;
	
	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String displayName;
	
	@Column(nullable = false)
	private String password;
	
	@ManyToMany
	@JoinTable(name = "course_user",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "course_id"))
	private List<Course> courses;
	
	@ManyToMany
	@JoinTable(name = "user_marked", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "marked_id"))
	private Set<WordSet> marked;
	
	@ElementCollection
	@MapKeyColumn(name = "meta_key")
	@Column(name = "meta_value")
	@CollectionTable(name = "user_meta", joinColumns = @JoinColumn(name = "user_id"))
	private Map<String, String> meta;
	
	@Enumerated(EnumType.STRING)
	private Role role;
	
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
	
	public void set(MetaKey key, Object obj) {
		getMeta().put(key.getMapKey(), obj.toString());
	}
	
	public String get(MetaKey key) {
		return getMeta().get(key);
	}
	
	@JsonIgnore
	@Override
	public @NotNull Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>(role.getPermissions());
		grantedAuthorities.add(role.getGrantedAuthority());
		
		return grantedAuthorities;
	}

	@Override
	public int hashCode() {
		int result = (int) (getId() ^ (getId() >>> 32));

		result = 31 * result + (getUsername() != null ? getUsername().hashCode() : 0);
		result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
		result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
		result = 31 * result + (getMarked() != null ? getMarked().hashCode() : 0);
		result = 31 * result + (getMeta() != null ? getMeta().hashCode() : 0);
		result = 31 * result + (getRole() != null ? getRole().hashCode() : 0);

		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		User user = (User) o;

		return getId() == user.getId();
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", username='" + username + '\'' +
				", role=" + role +
				'}';
	}
}
