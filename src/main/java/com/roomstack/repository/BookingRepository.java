package com.roomstack.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.roomstack.model.BookingModel;
import com.roomstack.model.CustomerModel;
import com.roomstack.model.RoomModel;
import com.roomstack.model.BookingModel.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<BookingModel, String> {
    
    /**
     * Find bookings by status
     * @param status The booking status to filter by
     * @return List of bookings with the specified status
     */
    List<BookingModel> findByStatus(BookingStatus status);
    
    /**
     * Find bookings for a specific room
     * @param room The room to find bookings for
     * @return List of bookings for the room
     */
    List<BookingModel> findByRoom(RoomModel room);
    
    /**
     * Find bookings for a specific customer
     * @param customer The customer to find bookings for
     * @return List of bookings for the customer
     */
    List<BookingModel> findByCustomer(CustomerModel customer);
    
    /**
     * Find bookings within a date range (check-in date)
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of bookings within the date range
     */
    List<BookingModel> findByCheckInBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find overlapping bookings for a room
     * @param roomId The room ID
     * @param checkIn The check-in date
     * @param checkOut The check-out date
     * @param bookingId Optional booking ID to exclude from the results (for updates)
     * @return List of overlapping bookings
     */
    @Query("SELECT b FROM BookingModel b WHERE b.room.id = :roomId " +
           "AND b.status != 'cancelled' " +
           "AND ((:checkIn BETWEEN b.checkIn AND b.checkOut) " +
           "OR (:checkOut BETWEEN b.checkIn AND b.checkOut) " +
           "OR (b.checkIn BETWEEN :checkIn AND :checkOut)) " +
           "AND (:bookingId IS NULL OR b.id != :bookingId)")
    List<BookingModel> findOverlappingBookings(
            @Param("roomId") String roomId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("bookingId") String bookingId);
    
    /**
     * Find current bookings for a room (status confirmed or checked_in)
     * @param room The room
     * @return List of current bookings
     */
    @Query("SELECT b FROM BookingModel b WHERE b.room = :room " +
           "AND (b.status = 'confirmed' OR b.status = 'checked_in')")
    List<BookingModel> findCurrentBookingsForRoom(@Param("room") RoomModel room);
    
    /**
     * Find bookings by check-in date
     * @param checkInDate The check-in date
     * @return List of bookings with the specified check-in date
     */
    @Query("SELECT b FROM BookingModel b WHERE DATE(b.checkIn) = DATE(:checkInDate)")
    List<BookingModel> findByCheckInDate(@Param("checkInDate") LocalDateTime checkInDate);
    
    /**
     * Find bookings by check-out date
     * @param checkOutDate The check-out date
     * @return List of bookings with the specified check-out date
     */
    @Query("SELECT b FROM BookingModel b WHERE DATE(b.checkOut) = DATE(:checkOutDate)")
    List<BookingModel> findByCheckOutDate(@Param("checkOutDate") LocalDateTime checkOutDate);
}
