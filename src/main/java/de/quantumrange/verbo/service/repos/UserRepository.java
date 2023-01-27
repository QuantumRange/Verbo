package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.Role;
import de.quantumrange.verbo.model.User;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.security.Principal;
import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
	@Query("select (count(u) > 0) from user u")
	boolean existsAny();
	@Query("select (count(u) > 0) from user u where u.username = ?1")
	boolean existsUsername(String username);


	default Optional<User> findByPrinciple(@Nullable Principal principal) {
		if (principal == null) return Optional.empty();
		if (principal.getName() == null) return Optional.empty();

		return findByUsername(principal.getName());
	}

	@NonNull
	@Query("select u from user u where u.username = ?1")
	Optional<User> findByUsername(@NonNull String username);

	@Query("select u from user u order by u.role, u.username")
	List<User> findByAllOrderByRoleAscUsernameAsc();

	@Query("select u from user u where u.role = ?1")
	List<User> findByRole(Role role);
	
}
