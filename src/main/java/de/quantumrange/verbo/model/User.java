package de.quantumrange.verbo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Table;
import org.jetbrains.annotations.NotNull;
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
	
	private String displayName;
	
	@Column(nullable = false)
	private String password;
	
	@ManyToMany
	@JoinTable(name = "user_marked", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "marked_id"))
	private Set<Word> marked;
	
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
	
	@JsonIgnore
	@Override
	public @NotNull Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>(role.getPermissions());
		grantedAuthorities.add(role.getGrantedAuthority());
		
		return grantedAuthorities;
	}
	
}
