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

import com.roomstack.model.CustomerModel;
import com.roomstack.repository.CustomerRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*") // For development; restrict in production
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Get all customers with optional filtering
     * @param status Optional filter by customer status (current/past)
     * @param search Optional search term for name, email, or phone
     * @return List of customers matching criteria
     */
    @GetMapping
    public ResponseEntity<List<CustomerModel>> getAllCustomers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        List<CustomerModel> customers;
        
        // If we have both status and search filters
        if (status != null && search != null && !search.isEmpty()) {
            Boolean isCurrentGuest = "current".equalsIgnoreCase(status);
            customers = customerRepository.searchCustomersByStatusAndTerm(search, isCurrentGuest);
        }
        // If we have only status filter
        else if (status != null) {
            Boolean isCurrentGuest = "current".equalsIgnoreCase(status);
            customers = customerRepository.findByCurrentGuest(isCurrentGuest);
        }
        // If we have only search filter
        else if (search != null && !search.isEmpty()) {
            customers = customerRepository.searchCustomers(search);
        }
        // No filters, return all customers
        else {
            customers = customerRepository.findAll();
        }
        
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }
    
    /**
     * Get a customer by ID
     * @param id The customer ID
     * @return The customer if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerModel> getCustomerById(@PathVariable String id) {
        return customerRepository.findById(id)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Create a new customer
     * @param customer The customer data
     * @return The created customer
     */
    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerModel customer) {
        // Check if email already exists
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409 Conflict
        }
        
        CustomerModel savedCustomer = customerRepository.save(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing customer
     * @param id The customer ID
     * @param customerDetails The updated customer data
     * @return The updated customer
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerModel customerDetails) {
        
        return customerRepository.findById(id)
                .map(existingCustomer -> {
                    // Check if email is being changed and already exists
                    if (!existingCustomer.getEmail().equals(customerDetails.getEmail()) && 
                            customerRepository.findByEmail(customerDetails.getEmail()).isPresent()) {
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "Email already exists");
                        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
                    }
                    
                    // Update customer properties
                    existingCustomer.setName(customerDetails.getName());
                    existingCustomer.setEmail(customerDetails.getEmail());
                    existingCustomer.setPhone(customerDetails.getPhone());
                    existingCustomer.setAddress(customerDetails.getAddress());
                    existingCustomer.setCurrentGuest(customerDetails.getCurrentGuest());
                    
                    CustomerModel updatedCustomer = customerRepository.save(existingCustomer);
                    return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Delete a customer
     * @param id The customer ID
     * @return Success or error status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteCustomer(@PathVariable String id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customerRepository.delete(customer);
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("deleted", Boolean.TRUE);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
