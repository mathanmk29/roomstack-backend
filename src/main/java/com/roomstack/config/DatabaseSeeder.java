package com.roomstack.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.roomstack.model.RoomModel;
import com.roomstack.repository.RoomRepository;

@Configuration
public class DatabaseSeeder {
    
    @Autowired
    private RoomRepository roomRepository;
    
    /**
     * Initialize database with sample room data for development
     * Only runs in 'dev' profile
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner initDatabase() {
        return args -> {
            // Only seed if the database is empty
            if (roomRepository.count() == 0) {
                System.out.println("Seeding database with sample rooms...");
                
                // Sample Room 1
                RoomModel room1 = new RoomModel();
                room1.setNumber("101");
                Map<String, Integer> beds1 = new HashMap<>();
                beds1.put("single", 1);
                beds1.put("double", 1);
                room1.setBeds(beds1);
                room1.setCapacity(3);
                room1.setPricePerNight(120);
                room1.setFeatures(Arrays.asList("TV", "WiFi", "Air Conditioning", "Mini Bar"));
                room1.setStatus("available");
                room1.setFloor(1);
                room1.setDescription("A comfortable room with modern amenities.");
                
                // Sample Room 2
                RoomModel room2 = new RoomModel();
                room2.setNumber("102");
                Map<String, Integer> beds2 = new HashMap<>();
                beds2.put("double", 1);
                room2.setBeds(beds2);
                room2.setCapacity(2);
                room2.setPricePerNight(180);
                room2.setFeatures(Arrays.asList("TV", "WiFi", "Air Conditioning", "Mini Bar", "Room Service", "Sea View"));
                room2.setStatus("occupied");
                room2.setFloor(1);
                room2.setDescription("A luxurious room with sea view.");
                
                // Sample Room 3
                RoomModel room3 = new RoomModel();
                room3.setNumber("201");
                Map<String, Integer> beds3 = new HashMap<>();
                beds3.put("double", 1);
                beds3.put("single", 1);
                room3.setBeds(beds3);
                room3.setCapacity(3);
                room3.setPricePerNight(250);
                room3.setFeatures(Arrays.asList("TV", "WiFi", "Air Conditioning", "Mini Bar", 
                                               "Room Service", "Mountain View", "Jacuzzi"));
                room3.setStatus("available");
                room3.setFloor(2);
                room3.setDescription("An elegant room with mountain view and private jacuzzi.");
                
                // Save all rooms
                roomRepository.saveAll(Arrays.asList(room1, room2, room3));
                
                System.out.println("Database seeded successfully!");
            }
        };
    }
}
