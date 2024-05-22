package org.example.couponcore.service;

import org.example.couponcore.TestConfig;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.model.CouponIssue;
import org.example.couponcore.model.CouponType;
import org.example.couponcore.repository.mysql.CouponIssueJpaRepository;
import org.example.couponcore.repository.mysql.CouponIssueRepository;
import org.example.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.example.couponcore.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

class CouponIssueServiceTest extends TestConfig {

    @Autowired
    CouponIssueService couponIssueService;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @BeforeEach
    void clean() {
        couponJpaRepository.deleteAllInBatch();
        couponIssueJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하면 예외반환")
    void saveCouponIssue_1() {
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();
        couponIssueJpaRepository.save(couponIssue);

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            couponIssueService.saveCouponIssue(couponIssue.getCouponId(), couponIssue.getUserId());
        });
        Assertions.assertEquals(exception.getErrorCode(), DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰내역이 존재하지 않는경우 쿠폰 발급")
    void saveCouponIssue_2() {

        long couponId = 1L;
        long userId = 1L;

        CouponIssue issue = couponIssueService.saveCouponIssue(couponId, userId);

        Assertions.assertTrue(couponIssueJpaRepository.findById(issue.getId()).isPresent());
    }

    @Test
    @DisplayName("발급수량, 기한, 중복 발급이 없는경우 쿠폰을 발급한다.")
    void issue_1() {
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SAVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(85)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .build();

        couponJpaRepository.save(coupon);

        long userId = 1L;

        couponIssueService.issue(coupon.getId(), userId);

        Coupon couponResult = couponJpaRepository.findById(coupon.getId()).get();
        Assertions.assertEquals(couponResult.getIssuedQuantity(), 86);

        CouponIssue couponIssueResult = couponIssueRepository.findFirstCouponIssue(coupon.getId(), userId);
        Assertions.assertNotNull(couponIssueResult);
    }

    @Test
    @DisplayName("발급수량에 문제가 생길 경우 예외발생")
    void issue_2() {
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SAVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .build();

        couponJpaRepository.save(coupon);

        long userId = 1L;
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            couponIssueService.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급기간에 문제가 생길 경우 예외발생")
    void issue_3() {
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SAVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(20)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .build();

        couponJpaRepository.save(coupon);

        long userId = 1L;
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            couponIssueService.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_DATE);

    }

    @Test
    @DisplayName("중복발급 될 경우 예외발생")
    void issue_4() {

        long userId = 1L;

        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SAVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(20)
                .dateIssueStart(LocalDateTime.now().minusDays(5))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .build();
        couponJpaRepository.save(coupon);

        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(userId)
                .build();
        couponIssueJpaRepository.save(couponIssue);


        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            couponIssueService.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), DUPLICATED_COUPON_ISSUE);

    }

    @Test
    @DisplayName("쿠폰이 없는경우 예외발생")
    void issue_5() {

        long userId = 1L;
        long couponId = 1L;

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            couponIssueService.issue(couponId, userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), COUPON_NOT_EXIST);

    }
}