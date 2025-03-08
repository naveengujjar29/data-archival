package com.auth.authservice.dto;

import com.auth.authservice.enums.Role;

/**
 * @author Naveen Kumar
 */
public class AssignRoleRequestDto {

    private String userName;
    private Role role;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
