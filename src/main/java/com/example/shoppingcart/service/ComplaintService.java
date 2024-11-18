package com.example.shoppingcart.service;

import com.example.shoppingcart.models.Complaints;
import com.example.shoppingcart.repo.ComplaintsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public  class ComplaintService {

    @Autowired
    private ComplaintsRepo complaintsRepo;

    //    USER
    public Complaints register(String userId, String description){
        Complaints complaints=new Complaints();
        complaints.setUserId(userId);
        complaints.setDescription(description);
        complaints.setCreatedAt(LocalDateTime.now());

        return complaintsRepo.save(complaints);
    }


    //    ADMIN
    public Complaints response(String complaintId,String response) throws Exception {
        Optional<Complaints> complaint=complaintsRepo.findById(complaintId);
        if(complaint.isPresent()) {
            Complaints complaints = complaint.get();
            complaints.setResponse(response);
            complaints.setStatus("RESOLVED");
            complaints.setUpdatedAt(LocalDateTime.now());
            return complaintsRepo.save(complaints);
        }
        else{
            throw new Exception("Complaint not found");
        }
    }

    //   ADMIN
    public List<Complaints> viewComplaints(){
        return complaintsRepo.findAll();
    }

    //    ADMIN
    public Optional<Complaints> viewById(String userId){
        return complaintsRepo.findById(userId);
    }


    public List<Complaints> getUserComplaints(String userId) {
        return complaintsRepo.findByUserId(userId);
    }

}