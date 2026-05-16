package com.voo.airline.service;

import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.dto.response.PassengerResponse;

import java.util.List;

public interface PassengerService {

    PassengerResponse findById(Long id);

    PassengerResponse findByCpf(String cpf);

    List<BookingResponse> findBookingsByCpf(String cpf);
}
