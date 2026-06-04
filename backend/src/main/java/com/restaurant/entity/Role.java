package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "role")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 設定主鍵自動遞增流水號
    @Column(name = "role_id")
    private Long roleId; // 1代表顧客，2代表員工

    @Column(name = "role_name")
    private String roleName; // 身份名稱（例如："STAFF"、"MEMBER"）

    @Column(name = "description")
    private String description; // 身份描述（例如："線上訂位會員"、"餐廳工作人員"）
}