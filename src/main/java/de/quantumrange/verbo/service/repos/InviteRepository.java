package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {
	@Query("select i from invite i where i.code = ?1")
	Optional<Invite> findByCode(String code);
}
