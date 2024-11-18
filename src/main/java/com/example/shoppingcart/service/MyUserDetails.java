package com.example.shoppingcart.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;



public class MyUserDetails implements UserDetails {


//    List<String> grantedAuthority = new ArrayList<>();
//    String password;
//    String userName;
//
//    public MyUserDetails(String grantedAuthority, String userName, String password){
//
//        this.grantedAuthority.add(grantedAuthority);
//        this.userName = userName;
//        this.password = password;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return Collections.singleton(new SimpleGrantedAuthority("USER"));
//    }
//    private List<String> roles;
//    private String password;
//    private String username;
//
//    public MyUserDetails(List<String> roles, String username, String password) {
//        this.roles = roles;
//        this.username = username;
//        this.password = password;
//    }

    private final Collection<GrantedAuthority> authorities; // Use Collection of GrantedAuthority
    private final String password;
    private final String username;

    // Constructor accepts Collection<GrantedAuthority>
    public MyUserDetails(Collection<GrantedAuthority> authorities, String username, String password) {
        this.authorities = authorities;
        this.username = username;
        this.password = password;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//       return authorities; // Return the stored authorities
        return this.authorities;

    }

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        // Convert role strings to GrantedAuthority objects
//        return roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
