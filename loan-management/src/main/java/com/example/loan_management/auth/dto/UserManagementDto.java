
package com.example.loan_management.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserManagementDto {

    private Long id;
    private String name;
    private String email;
    private String role;
    private Long cmrcId;
    private String cmrcName;
}
