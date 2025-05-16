package com.roomstack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomstack.model.BillModel;
import com.roomstack.model.BookingModel;
import com.roomstack.model.CustomerModel;
import com.roomstack.model.RoomModel;
import com.roomstack.model.BillModel.PaymentStatus;
import com.roomstack.model.BookingModel.BookingStatus;
import com.roomstack.repository.BillRepository;
import com.roomstack.repository.BookingRepository;
import com.roomstack.repository.CustomerRepository;
import com.roomstack.repository.RoomRepository;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BillRepository billRepository;
    
    /**
     * Get all bookings
     */
    public List<BookingModel> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    /**
     * Get booking by ID
     */
    public BookingModel getBookingById(String id) {
        return bookingRepository.findById(id).orElse(null);
    }
    
    /**
     * Create a new booking and associated bill
     */
    @Transactional
    public BookingModel createBooking(BookingModel booking, String roomId, String customerId) {
        // Get room and customer
        RoomModel room = roomRepository.findById(roomId).orElse(null);
        CustomerModel customer = customerRepository.findById(customerId).orElse(null);
        
        if (room == null || customer == null) {
            return null;
        }
        
        // Set room and customer
        booking.setRoom(room);
        booking.setCustomer(customer);
        
        // Set status to confirmed
        booking.setStatus(BookingStatus.confirmed);
        
        // Update room status
        room.setStatus("occupied");
        roomRepository.save(room);
        
        // Save booking
        BookingModel savedBooking = bookingRepository.save(booking);
        
        // Calculate and create bill
        BillModel bill = calculateBill(booking, room.getPricePerNight());
        bill.setBooking(savedBooking);
        bill.setPaymentStatus(PaymentStatus.pending);
        billRepository.save(bill);
        
        return savedBooking;
    }
    
    /**
     * Update booking status
     */
    @Transactional
    public BookingModel updateBookingStatus(String id, BookingStatus status) {
        BookingModel booking = bookingRepository.findById(id).orElse(null);
        
        if (booking == null) {
            return null;
        }
        
        booking.setStatus(status);
        
        // Update room status based on booking status
        RoomModel room = booking.getRoom();
        switch (status) {
            case confirmed:
                room.setStatus("reserved");
                break;
            case checked_in:
                room.setStatus("occupied");
                break;
            case checked_out:
            case cancelled:
                room.setStatus("available");
                break;
        }
        
        roomRepository.save(room);
        return bookingRepository.save(booking);
    }
    
    /**
     * Delete a booking
     */
    @Transactional
    public boolean deleteBooking(String id) {
        BookingModel booking = bookingRepository.findById(id).orElse(null);
        
        if (booking == null) {
            return false;
        }
        
        // Update room status if booking is active
        if (booking.getStatus() == BookingStatus.confirmed || booking.getStatus() == BookingStatus.checked_in) {
            RoomModel room = booking.getRoom();
            room.setStatus("available");
            roomRepository.save(room);
        }
        
        bookingRepository.delete(booking);
        return true;
    }
    
    /**
     * Calculate bill from booking details
     */
    private BillModel calculateBill(BookingModel booking, double pricePerNight) {
        // Calculate number of nights
        long nights = ChronoUnit.DAYS.between(
                booking.getCheckIn().toLocalDate(), 
                booking.getCheckOut().toLocalDate());
        
        // Ensure at least 1 night is charged
        nights = Math.max(1, nights);
        
        // Calculate room charge
        BigDecimal roomCharge = BigDecimal.valueOf(pricePerNight)
                .multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate tax (10%)
        BigDecimal taxAmount = roomCharge.multiply(BigDecimal.valueOf(0.1))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate total
        BigDecimal total = roomCharge.add(taxAmount);
        
        BillModel bill = new BillModel();
        bill.setRoomCharge(roomCharge);
        bill.setTaxAmount(taxAmount);
        bill.setTotal(total);
        
        return bill;
    }
    
    /**
     * Check if room is available for the given dates
     */
    public boolean isRoomAvailable(String roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        List<BookingModel> overlaps = bookingRepository.findOverlappingBookings(
                roomId, checkIn, checkOut, null);
        
        return overlaps.isEmpty();
    }
    
    /**
     * Get bookings by status
     */
    public List<BookingModel> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }
    
    /**
     * Get bookings for a room
     */
    public List<BookingModel> getBookingsByRoom(String roomId) {
        RoomModel room = roomRepository.findById(roomId).orElse(null);
        
        if (room == null) {
            return List.of();
        }
        
        return bookingRepository.findByRoom(room);
    }
    
    /**
     * Get bookings for a customer
     */
    public List<BookingModel> getBookingsByCustomer(String customerId) {
        CustomerModel customer = customerRepository.findById(customerId).orElse(null);
        
        if (customer == null) {
            return List.of();
        }
        
        return bookingRepository.findByCustomer(customer);
    }
}
