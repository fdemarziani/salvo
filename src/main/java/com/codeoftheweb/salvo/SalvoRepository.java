package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface SalvoRepository extends JpaRepository<Salvo, Long> {
    List<Salvo> findByTurn (@Param("turn") long turn);
}