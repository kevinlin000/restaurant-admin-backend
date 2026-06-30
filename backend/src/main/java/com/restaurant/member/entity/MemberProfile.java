package com.restaurant.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 顧客會員實體，對應 members 資料表。
 * 掛載於 User 主表之下，負責記錄顧客的可用折抵點數、會員升級點數與會員等級。
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfile {
    public enum MemberLevel {
        BRONZE, SILVER, GOLD, DIAMOND
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 可用折抵點數：訂單折抵時會扣除，取消訂單退點時會加回。
     */
    @Column(name = "point_balance", nullable = false)
    @Builder.Default
    private Integer pointBalance = 0;

    /**
     * 會員升級點數：只用來判斷會員等級，不會因點數折抵或退點而減少。
     */
    @Column(name = "point_level", nullable = false)
    @Builder.Default
    private Integer pointLevel = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_level", nullable = false, columnDefinition = "varchar(20)")
    @Builder.Default
    private MemberLevel memberLevel = MemberLevel.BRONZE;
}