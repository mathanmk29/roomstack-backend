package com.roomstack.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.roomstack.model.BillModel;
import com.roomstack.model.BillModel.PaymentStatus;
import com.roomstack.service.BillService;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*") // For development; restrict in production
public class BillController {

    @Autowired
    private BillService billService;
    
    /**
     * Get all bills with optional filtering
     */
    @GetMapping
    public ResponseEntity<List<BillModel>> getAllBills(
            @RequestParam(required = false) String status) {
        
        List<BillModel> bills;
        
        if (status != null) {
            try {
                PaymentStatus paymentStatus = PaymentStatus.valueOf(status);
                bills = billService.getBillsByPaymentStatus(paymentStatus);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            bills = billService.getAllBills();
        }
        
        return new ResponseEntity<>(bills, HttpStatus.OK);
    }
    
    /**
     * Get a bill by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BillModel> getBillById(@PathVariable String id) {
        BillModel bill = billService.getBillById(id);
        
        if (bill == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(bill, HttpStatus.OK);
    }
    
    /**
     * Get bill by booking ID
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<BillModel> getBillByBookingId(@PathVariable String bookingId) {
        BillModel bill = billService.getBillByBookingId(bookingId);
        
        if (bill == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(bill, HttpStatus.OK);
    }
    
    /**
     * Update payment status
     */
    @PutMapping("/{id}/payment")
    public ResponseEntity<BillModel> updatePaymentStatus(
            @PathVariable String id,
            @RequestParam String status) {
        
        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        BillModel updatedBill = billService.updatePaymentStatus(id, paymentStatus);
        
        if (updatedBill == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(updatedBill, HttpStatus.OK);
    }
    
    /**
     * Get available payment statuses
     */
    @GetMapping("/payment-statuses")
    public ResponseEntity<Map<String, String>> getPaymentStatuses() {
        Map<String, String> statuses = new HashMap<>();
        statuses.put("pending", "Pending");
        statuses.put("partial", "Partially Paid");
        statuses.put("paid", "Fully Paid");
        
        return new ResponseEntity<>(statuses, HttpStatus.OK);
    }
}
