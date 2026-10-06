package com.mx.mitienda.repository;

import com.mx.mitienda.model.ReservaBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface ReservaBotRepository extends JpaRepository<ReservaBot, Long> {

    // Devuelve true si ya existe una reserva que choca con este horario en esa sucursal
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM ReservaBot r WHERE r.sucursal.id = :sucursalId " +
            "AND r.estado = 'CONFIRMADO' " +
            "AND r.horaInicio < :horaFin AND r.horaFin > :horaInicio")
    boolean existeSolapamiento(
            @Param("sucursalId") Long sucursalId,
            @Param("horaInicio") LocalDateTime horaInicio,
            @Param("horaFin") LocalDateTime horaFin
    );
}