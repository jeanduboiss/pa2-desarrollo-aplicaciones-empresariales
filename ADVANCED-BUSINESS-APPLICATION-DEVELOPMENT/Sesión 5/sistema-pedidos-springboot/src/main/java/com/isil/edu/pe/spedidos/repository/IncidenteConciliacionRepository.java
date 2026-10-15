package com.isil.edu.pe.spedidos.repository;

import com.isil.edu.pe.spedidos.entity.IncidenteConciliacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidenteConciliacionRepository extends JpaRepository<IncidenteConciliacion, Long> {

}
