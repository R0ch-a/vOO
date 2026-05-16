package com.voo.airline.repository;

import com.voo.airline.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    Optional<Passenger> findByCpf(String cpf);

    Optional<Passenger> findByEmail(String email);

    boolean existsByCpf(String cpf);
}
