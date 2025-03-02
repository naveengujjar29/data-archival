package com.auth.authservice.service;

import com.auth.authservice.enums.Role;
import com.auth.authservice.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private String userName;

    private String password;

    private Role role;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(String userName, String password, Collection<? extends GrantedAuthority> authorities,
                           Role role) {
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
        this.role = role;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(user.getRole().toString()));
        return new UserDetailsImpl(
                user.getUsername(),
                user.getPassword(),
                authorities, user.getRole());
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }





    public Role getRole() {
        return role;
    }
}
