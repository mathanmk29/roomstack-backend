package com.roomstack.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.roomstack.model.RoomModel;
import com.roomstack.repository.RoomRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*") // For development; restrict in production
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;
    
    /**
     * Get all rooms with optional filtering
     * @param status Optional filter by room status
     * @param floor Optional filter by floor
     * @param minCapacity Optional filter by minimum capacity
     * @return List of rooms matching criteria
     */
    @GetMapping
    public ResponseEntity<List<RoomModel>> getAllRooms(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer minCapacity) {
        
        List<RoomModel> rooms;
        
        if (status != null) {
            rooms = roomRepository.findByStatus(status);
        } else if (floor != null) {
            rooms = roomRepository.findByFloor(floor);
        } else if (minCapacity != null) {
            rooms = roomRepository.findByCapacityGreaterThanEqual(minCapacity);
        } else {
            rooms = roomRepository.findAll();
        }
        
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }
    
    /**
     * Get a room by ID
     * @param id The room ID
     * @return The room if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomModel> getRoomById(@PathVariable String id) {
        return roomRepository.findById(id)
                .map(room -> new ResponseEntity<>(room, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Create a new room
     * @param room The room data
     * @return The created room
     */
    @PostMapping
    public ResponseEntity<RoomModel> createRoom(@Valid @RequestBody RoomModel room) {
        // Check if room number already exists
        if (roomRepository.findByNumber(room.getNumber()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409 Conflict
        }
        
        RoomModel savedRoom = roomRepository.save(room);
        return new ResponseEntity<>(savedRoom, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing room
     * @param id The room ID
     * @param roomDetails The updated room data
     * @return The updated room
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoomModel> updateRoom(
            @PathVariable String id,
            @Valid @RequestBody RoomModel roomDetails) {
        
        return roomRepository.findById(id)
                .map(existingRoom -> {
                    // Check if number is being changed and already exists
                    if (!existingRoom.getNumber().equals(roomDetails.getNumber()) && 
                            roomRepository.findByNumber(roomDetails.getNumber()).isPresent()) {
                        return new ResponseEntity<RoomModel>(HttpStatus.CONFLICT);
                    }
                    
                    // Update room properties
                    existingRoom.setNumber(roomDetails.getNumber());
                    existingRoom.setBeds(roomDetails.getBeds());
                    existingRoom.setCapacity(roomDetails.getCapacity());
                    existingRoom.setPricePerNight(roomDetails.getPricePerNight());
                    existingRoom.setFeatures(roomDetails.getFeatures());
                    existingRoom.setStatus(roomDetails.getStatus());
                    existingRoom.setFloor(roomDetails.getFloor());
                    existingRoom.setDescription(roomDetails.getDescription());
                    
                    RoomModel updatedRoom = roomRepository.save(existingRoom);
                    return new ResponseEntity<>(updatedRoom, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Delete a room
     * @param id The room ID
     * @return Success or error status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteRoom(@PathVariable String id) {
        return roomRepository.findById(id)
                .map(room -> {
                    roomRepository.delete(room);
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("deleted", Boolean.TRUE);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Get available room statuses
     * @return List of valid room statuses
     */
    @GetMapping("/statuses")
    public ResponseEntity<Map<String, String>> getRoomStatuses() {
        Map<String, String> statuses = new HashMap<>();
        statuses.put("available", "Available");
        statuses.put("occupied", "Occupied");
        statuses.put("maintenance", "Under Maintenance");
        statuses.put("reserved", "Reserved");
        
        return new ResponseEntity<>(statuses, HttpStatus.OK);
    }
}
