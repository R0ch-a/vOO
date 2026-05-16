package com.voo.airline.service;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    BookingResponse create(CreateBookingRequest request);

    BookingResponse findByLocator(String locator);

    Page<BookingResponse> findAll(Pageable pageable);

    BookingResponse cancel(String locator);
}
