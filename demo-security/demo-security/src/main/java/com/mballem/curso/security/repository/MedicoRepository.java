package com.mballem.curso.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mballem.curso.security.domain.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Long>{
	
	@Query("select m from Medico m where m.usuario.id = :id")
	Optional<Medico> findByUsuarioId(Long id);
}
