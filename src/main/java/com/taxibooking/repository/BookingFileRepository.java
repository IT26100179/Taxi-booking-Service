package com.taxibooking.repository;

import com.taxibooking.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * File Repository for Ride Bookings (Component 4 - Ride Booking & Trip Management)
 */
@Repository
public class BookingFileRepository extends AbstractFileRepository<Booking, String> {

    public BookingFileRepository(@Value("${app.data.bookings-file:data/bookings.txt}") String filePath) {
        super(filePath);
    }

    @Override
    protected String getId(Booking entity) {
        return entity.getBookingId();
    }

    @Override
    protected String serialize(Booking entity) {
        return entity.toFileString();
    }

    @Override
    protected Booking deserialize(String line) {
        return Booking.fromFileString(line);
    }
}
