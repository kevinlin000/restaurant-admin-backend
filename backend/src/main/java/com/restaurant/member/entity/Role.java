package com.restaurant.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 系統權限角色實體，對應資料庫的 role 資料表。
 * 用於定義系統內的所有使用者身份權限（例如：MEMBER, ADMIN, MANAGER, CHEF）。
 */
@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 20)
    private String roleName;

    @Column(name = "description", length = 200)
    private String description;
}