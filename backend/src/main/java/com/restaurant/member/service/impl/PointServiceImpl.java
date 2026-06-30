package com.restaurant.member.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.member.dto.PointBalanceResponse;
import com.restaurant.member.dto.PointTransactionResponse;
import com.restaurant.member.entity.MemberProfile;
import com.restaurant.member.entity.PointTransaction;
import com.restaurant.member.entity.User;
import com.restaurant.member.entity.MemberProfile.MemberLevel;
import com.restaurant.member.repository.MemberProfileRepository;
import com.restaurant.member.repository.PointTransactionRepository;
import com.restaurant.member.repository.UserRepository;
import com.restaurant.member.service.PointService;
import com.restaurant.store.entity.Store;
import com.restaurant.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private static final int POINT_EARN_AMOUNT = 100;
    private static final int SILVER_THRESHOLD = 30;
    private static final int GOLD_THRESHOLD = 60;
    private static final int DIAMOND_THRESHOLD = 100;

    private final UserRepository userRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public PointBalanceResponse getPointBalance(Long userId) {
        MemberProfile profile = getMemberProfile(userId);
        int discountPoints = safePoints(profile.getPointBalance());
        int levelPoints = safePoints(profile.getPointLevel());
        MemberLevel currentLevel = calculateMemberLevel(levelPoints);

        return PointBalanceResponse.builder()
                .pointBalance(discountPoints)
                .pointLevel(levelPoints)
                .memberLevel(currentLevel)
                .nextLevel(getNextLevel(levelPoints))
                .pointsToNextLevel(getPointsToNextLevel(levelPoints))
                .earnRuleText("每消費 $100 即可累積 1 點")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransactionResponse> getPointHistory(Long userId) {
        getMemberProfile(userId);

        return pointTransactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toPointTransactionResponse)
                .toList();
    }

    @Override
    @Transactional
    public int earnPointsFromOrder(Long userId, Long storeId, Long orderId, BigDecimal totalAmount) {
        if (userId == null || totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        int pointsEarned = totalAmount
                .divide(BigDecimal.valueOf(POINT_EARN_AMOUNT), 0, RoundingMode.DOWN)
                .intValue();

        if (pointsEarned <= 0) {
            return 0;
        }

        User user = getUser(userId);
        MemberProfile profile = getMemberProfile(userId);
        Store store = getStoreOrNull(storeId);

        if (orderId != null && pointTransactionRepository
                .existsByReferenceIdAndTransactionType(orderId, PointTransaction.TransactionType.EARN)) {
            return 0;
        }

        int updatedDiscountPoints = safePoints(profile.getPointBalance()) + pointsEarned;
        int updatedLevelPoints = safePoints(profile.getPointLevel()) + pointsEarned;

        profile.setPointBalance(updatedDiscountPoints);
        profile.setPointLevel(updatedLevelPoints);
        profile.setMemberLevel(calculateMemberLevel(updatedLevelPoints));
        memberProfileRepository.save(profile);

        pointTransactionRepository.save(PointTransaction.builder()
                .user(user)
                .store(store)
                .pointChange(pointsEarned)
                .transactionType(PointTransaction.TransactionType.EARN)
                .referenceId(orderId)
                .build());

        return pointsEarned;
    }

    @Override
    @Transactional
    public int usePointsForOrder(Long userId, Long storeId, Long orderId, Integer pointsToUse) {
        if (userId == null || pointsToUse == null || pointsToUse <= 0) {
            return 0;
        }

        User user = getUser(userId);
        MemberProfile profile = getMemberProfile(userId);
        Store store = getStoreOrNull(storeId);
        int currentPoints = safePoints(profile.getPointBalance());

        if (currentPoints < pointsToUse) {
            throw new BusinessException("折抵點數不足");
        }

        if (orderId != null && pointTransactionRepository
                .existsByReferenceIdAndTransactionType(orderId, PointTransaction.TransactionType.USE)) {
            return 0;
        }

        profile.setPointBalance(currentPoints - pointsToUse);
        // 折抵只扣 point_balance，不動 point_level；會員等級仍以 point_level 判斷。
        profile.setMemberLevel(calculateMemberLevel(safePoints(profile.getPointLevel())));
        memberProfileRepository.save(profile);

        pointTransactionRepository.save(PointTransaction.builder()
                .user(user)
                .store(store)
                .pointChange(-pointsToUse)
                .transactionType(PointTransaction.TransactionType.USE)
                .referenceId(orderId)
                .build());

        return pointsToUse;
    }

    @Override
    @Transactional
    public int refundPointsForOrder(Long userId, Long storeId, Long orderId, Integer pointsToRefund) {
        if (userId == null || pointsToRefund == null || pointsToRefund <= 0) {
            return 0;
        }

        User user = getUser(userId);
        MemberProfile profile = getMemberProfile(userId);
        Store store = getStoreOrNull(storeId);

        if (orderId != null && pointTransactionRepository
                .existsByReferenceIdAndTransactionType(orderId, PointTransaction.TransactionType.REFUND)) {
            return 0;
        }

        profile.setPointBalance(safePoints(profile.getPointBalance()) + pointsToRefund);
        // 取消訂單只退回可用折抵點數，不增加會員升級點數。
        profile.setMemberLevel(calculateMemberLevel(safePoints(profile.getPointLevel())));
        memberProfileRepository.save(profile);

        pointTransactionRepository.save(PointTransaction.builder()
                .user(user)
                .store(store)
                .pointChange(pointsToRefund)
                .transactionType(PointTransaction.TransactionType.REFUND)
                .referenceId(orderId)
                .build());

        return pointsToRefund;
    }

    @Override
    @Transactional
    public void updateMemberLevel(Long userId) {
        MemberProfile profile = getMemberProfile(userId);
        profile.setMemberLevel(calculateMemberLevel(safePoints(profile.getPointLevel())));
        memberProfileRepository.save(profile);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("使用者不存在"));
    }

    private MemberProfile getMemberProfile(Long userId) {
        User user = getUser(userId);

        return memberProfileRepository.findByUserUserId(userId)
                .orElseGet(() -> memberProfileRepository.save(
                        MemberProfile.builder()
                                .user(user)
                                .build()));
    }

    private Store getStoreOrNull(Long storeId) {
        if (storeId == null) {
            return null;
        }
        Optional<Store> store = storeRepository.findById(storeId);
        return store.orElse(null);
    }

    private int safePoints(Integer points) {
        return points == null ? 0 : points;
    }

    private MemberLevel calculateMemberLevel(int points) {
        if (points >= DIAMOND_THRESHOLD) {
            return MemberLevel.DIAMOND;
        }
        if (points >= GOLD_THRESHOLD) {
            return MemberLevel.GOLD;
        }
        if (points >= SILVER_THRESHOLD) {
            return MemberLevel.SILVER;
        }
        return MemberLevel.BRONZE;
    }

    private MemberLevel getNextLevel(int points) {
        if (points < SILVER_THRESHOLD) {
            return MemberLevel.SILVER;
        }
        if (points < GOLD_THRESHOLD) {
            return MemberLevel.GOLD;
        }
        if (points < DIAMOND_THRESHOLD) {
            return MemberLevel.DIAMOND;
        }
        return null;
    }

    private Integer getPointsToNextLevel(int points) {
        if (points < SILVER_THRESHOLD) {
            return SILVER_THRESHOLD - points;
        }
        if (points < GOLD_THRESHOLD) {
            return GOLD_THRESHOLD - points;
        }
        if (points < DIAMOND_THRESHOLD) {
            return DIAMOND_THRESHOLD - points;
        }
        return 0;
    }

    private PointTransactionResponse toPointTransactionResponse(PointTransaction tx) {
        return PointTransactionResponse.builder()
                .txId(tx.getTxId())
                .pointChange(tx.getPointChange())
                .transactionType(tx.getTransactionType().name())
                .referenceId(tx.getReferenceId())
                .storeName(tx.getStore() != null ? tx.getStore().getStoreName() : null)
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
