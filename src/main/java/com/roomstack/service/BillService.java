package com.roomstack.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomstack.model.BillModel;
import com.roomstack.model.BillModel.PaymentStatus;
import com.roomstack.repository.BillRepository;

@Service
public class BillService {
    
    @Autowired
    private BillRepository billRepository;
    
    /**
     * Get all bills
     */
    public List<BillModel> getAllBills() {
        return billRepository.findAll();
    }
    
    /**
     * Get bill by ID
     */
    public BillModel getBillById(String id) {
        return billRepository.findById(id).orElse(null);
    }
    
    /**
     * Get bill by booking ID
     */
    public BillModel getBillByBookingId(String bookingId) {
        return billRepository.findByBookingId(bookingId).orElse(null);
    }
    
    /**
     * Update payment status
     */
    @Transactional
    public BillModel updatePaymentStatus(String id, PaymentStatus paymentStatus) {
        BillModel bill = billRepository.findById(id).orElse(null);
        
        if (bill == null) {
            return null;
        }
        
        bill.setPaymentStatus(paymentStatus);
        
        // Set payment date if status is paid
        if (paymentStatus == PaymentStatus.paid) {
            bill.setPaymentDate(LocalDateTime.now());
        }
        
        return billRepository.save(bill);
    }
    
    /**
     * Get bills by payment status
     */
    public List<BillModel> getBillsByPaymentStatus(PaymentStatus paymentStatus) {
        return billRepository.findByPaymentStatus(paymentStatus);
    }
    
    /**
     * Record payment for a bill
     */
    @Transactional
    public BillModel recordPayment(String id, PaymentStatus status) {
        BillModel bill = billRepository.findById(id).orElse(null);
        
        if (bill == null) {
            return null;
        }
        
        bill.setPaymentStatus(status);
        
        if (status == PaymentStatus.paid) {
            bill.setPaymentDate(LocalDateTime.now());
        }
        
        return billRepository.save(bill);
    }
}
