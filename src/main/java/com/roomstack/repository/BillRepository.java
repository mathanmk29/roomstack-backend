package com.roomstack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roomstack.model.BillModel;
import com.roomstack.model.BookingModel;
import com.roomstack.model.BillModel.PaymentStatus;

@Repository
public interface BillRepository extends JpaRepository<BillModel, String> {
    
    /**
     * Find bill by booking
     * @param booking The booking to find the bill for
     * @return Optional containing the bill if found
     */
    Optional<BillModel> findByBooking(BookingModel booking);
    
    /**
     * Find bills by payment status
     * @param paymentStatus The payment status to filter by
     * @return List of bills with the specified payment status
     */
    List<BillModel> findByPaymentStatus(PaymentStatus paymentStatus);
    
    /**
     * Find bills by booking ID
     * @param bookingId The booking ID to find the bill for
     * @return Optional containing the bill if found
     */
    Optional<BillModel> findByBookingId(String bookingId);
}
