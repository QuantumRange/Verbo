package de.quantumrange.verbo.service.repos;

import de.quantumrange.verbo.model.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRepository extends JpaRepository<Invite, Long> {
}
