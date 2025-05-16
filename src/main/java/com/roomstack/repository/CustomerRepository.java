package com.roomstack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.roomstack.model.CustomerModel;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerModel, String> {
    
    /**
     * Find a customer by email
     * @param email The email to search for
     * @return An Optional containing the customer if found
     */
    Optional<CustomerModel> findByEmail(String email);
    
    /**
     * Find customers by their current guest status
     * @param currentGuest True for current guests, false for past guests
     * @return List of customers with the specified status
     */
    List<CustomerModel> findByCurrentGuest(Boolean currentGuest);
    
    /**
     * Search customers by name, email, or phone containing the search term
     * @param searchTerm The search term to look for
     * @return List of matching customers
     */
    @Query("SELECT c FROM CustomerModel c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.phone LIKE CONCAT('%', :searchTerm, '%')")
    List<CustomerModel> searchCustomers(@Param("searchTerm") String searchTerm);
    
    /**
     * Search customers who are current guests by name, email, or phone
     * @param searchTerm The search term to look for
     * @param currentGuest The current guest status to filter by
     * @return List of matching customers
     */
    @Query("SELECT c FROM CustomerModel c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.phone LIKE CONCAT('%', :searchTerm, '%')) AND " +
           "c.currentGuest = :currentGuest")
    List<CustomerModel> searchCustomersByStatusAndTerm(
            @Param("searchTerm") String searchTerm, 
            @Param("currentGuest") Boolean currentGuest);
}
