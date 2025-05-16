package com.roomstack.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.roomstack.model.BookingModel;
import com.roomstack.model.BookingModel.BookingStatus;
import com.roomstack.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*") // For development; restrict in production
public class BookingController {

    @Autowired
    private BookingService bookingService;
    
    /**
     * Get all bookings with optional filtering
     */
    @GetMapping
    public ResponseEntity<List<BookingModel>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String customerId) {
        
        List<BookingModel> bookings;
        
        if (status != null) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status);
                bookings = bookingService.getBookingsByStatus(bookingStatus);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else if (roomId != null) {
            bookings = bookingService.getBookingsByRoom(roomId);
        } else if (customerId != null) {
            bookings = bookingService.getBookingsByCustomer(customerId);
        } else {
            bookings = bookingService.getAllBookings();
        }
        
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }
    
    /**
     * Get a booking by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingModel> getBookingById(@PathVariable String id) {
        BookingModel booking = bookingService.getBookingById(id);
        
        if (booking == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }
    
    /**
     * Create a new booking
     */
    @PostMapping
    public ResponseEntity<BookingModel> createBooking(
            @RequestBody Map<String, Object> bookingRequest,
            @RequestParam String roomId,
            @RequestParam String customerId) {
        
        // Extract booking data from request
        BookingModel booking = new BookingModel();
        
        try {
            // Set basic booking properties
            if (bookingRequest.get("checkIn") != null) {
                booking.setCheckIn(LocalDateTime.parse((String) bookingRequest.get("checkIn")));
            }
            
            if (bookingRequest.get("checkOut") != null) {
                booking.setCheckOut(LocalDateTime.parse((String) bookingRequest.get("checkOut")));
            }
            
            if (bookingRequest.get("adults") != null) {
                booking.setAdults(Integer.parseInt(bookingRequest.get("adults").toString()));
            }
            
            if (bookingRequest.get("children") != null) {
                booking.setChildren(Integer.parseInt(bookingRequest.get("children").toString()));
            }
            
            if (bookingRequest.get("specialRequests") != null) {
                booking.setSpecialRequests((String) bookingRequest.get("specialRequests"));
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        // Validate dates
        if (booking.getCheckIn() == null || booking.getCheckOut() == null || 
            booking.getCheckIn().isAfter(booking.getCheckOut())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        // Check availability
        if (!bookingService.isRoomAvailable(roomId, booking.getCheckIn(), booking.getCheckOut())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        
        BookingModel createdBooking = bookingService.createBooking(booking, roomId, customerId);
        
        if (createdBooking == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }
    
    /**
     * Update booking status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<BookingModel> updateBookingStatus(
            @PathVariable String id,
            @RequestParam String status) {
        
        BookingStatus bookingStatus;
        try {
            bookingStatus = BookingStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        BookingModel updatedBooking = bookingService.updateBookingStatus(id, bookingStatus);
        
        if (updatedBooking == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
    }
    
    /**
     * Delete a booking
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteBooking(@PathVariable String id) {
        boolean deleted = bookingService.deleteBooking(id);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        
        if (!deleted) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Get available booking statuses
     */
    @GetMapping("/statuses")
    public ResponseEntity<Map<String, String>> getBookingStatuses() {
        Map<String, String> statuses = new HashMap<>();
        statuses.put("confirmed", "Confirmed");
        statuses.put("checked_in", "Checked In");
        statuses.put("checked_out", "Checked Out");
        statuses.put("cancelled", "Cancelled");
        
        return new ResponseEntity<>(statuses, HttpStatus.OK);
    }
    
    /**
     * Check room availability
     */
    @GetMapping("/availability/check")
    public ResponseEntity<Map<String, Boolean>> checkRoomAvailability(
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut) {
        
        // Validate dates
        if (checkIn.isAfter(checkOut)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        boolean isAvailable = bookingService.isRoomAvailable(roomId, checkIn, checkOut);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
