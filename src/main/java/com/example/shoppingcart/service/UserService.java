package com.example.shoppingcart.service;


import com.example.shoppingcart.models.User;
import com.example.shoppingcart.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        User user=userRepo.findByUsername(username).orElse(null);
        System.out.println( "User roles: " + user.getRole());
        if(user==null){
            System.out.println("User is null");
            throw new UsernameNotFoundException("USER NOT FOUND");
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().toString().toUpperCase()));

        System.out.println("User roles: " + authorities);
        return new MyUserDetails(authorities, user.getUsername(), user.getPassword());

    }



}
