package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.User;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.security.Principal;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {


	default Optional<User> findByPrinciple(@Nullable Principal principal) {
		if (principal == null) return Optional.empty();
		if (principal.getName() == null) return Optional.empty();

		return findByUsername(principal.getName());
	}

	@NonNull
	@Query("select u from user u where u.username = ?1")
	Optional<User> findByUsername(@NonNull String username);



}
