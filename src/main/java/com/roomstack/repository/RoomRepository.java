package com.roomstack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roomstack.model.RoomModel;

@Repository
public interface RoomRepository extends JpaRepository<RoomModel, String> {
    
    /**
     * Find a room by its room number
     * @param number The room number to search for
     * @return An Optional containing the room if found
     */
    Optional<RoomModel> findByNumber(String number);
    
    /**
     * Find rooms by their status
     * @param status The room status to filter by
     * @return A list of rooms with the specified status
     */
    List<RoomModel> findByStatus(String status);
    
    /**
     * Find rooms on a specific floor
     * @param floor The floor number to filter by
     * @return A list of rooms on the specified floor
     */
    List<RoomModel> findByFloor(int floor);
    
    /**
     * Find rooms within a price range
     * @param minPrice The minimum price per night
     * @param maxPrice The maximum price per night
     * @return A list of rooms within the specified price range
     */
    List<RoomModel> findByPricePerNightBetween(double minPrice, double maxPrice);
    
    /**
     * Find rooms with a minimum capacity
     * @param capacity The minimum capacity required
     * @return A list of rooms with at least the specified capacity
     */
    List<RoomModel> findByCapacityGreaterThanEqual(int capacity);
}
