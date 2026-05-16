package com.voo.airline.service.impl;

import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.dto.response.PassengerResponse;
import com.voo.airline.exception.ResourceNotFoundException;
import com.voo.airline.repository.BookingRepository;
import com.voo.airline.repository.PassengerRepository;
import com.voo.airline.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository   bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public PassengerResponse findById(Long id) {
        return passengerRepository.findById(id)
            .map(PassengerResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Passageiro", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PassengerResponse findByCpf(String cpf) {
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        return passengerRepository.findByCpf(cpfLimpo)
            .map(PassengerResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Passageiro", "cpf", cpf));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findBookingsByCpf(String cpf) {
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        return bookingRepository.findByPassengerCpf(cpfLimpo).stream()
            .map(BookingResponse::from)
            .toList();
    }
}
