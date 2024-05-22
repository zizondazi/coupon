package org.example.couponcore.model;

import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @Test
    @DisplayName("발급 수량이 남아있다면 true 반환한다.")
    void availableIssueQuantity_1() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();

        boolean result = coupon.availableIssueQuantity();

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 수량이 남아있다면 false 반환한다.")
    void availableIssueQuantity_2() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(101)
                .build();

        boolean result = coupon.availableIssueQuantity();

        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급 수량이 설정되지 않았다면 true 반환한다.")
    void availableIssueQuantity_3() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(101)
                .build();

        boolean result = coupon.availableIssueQuantity();

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급기간 이전이면 flase 반환한다.")
    void availableIssueDate_1() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        boolean result = coupon.availableIssueDate();

        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급기간에 해당하면 true 반환한다.")
    void availableIssueDate_2() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        boolean result = coupon.availableIssueDate();

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급기간 종료되면 flase 반환한다.")
    void availableIssueDate_3() {
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().minusDays(2))
                .build();

        boolean result = coupon.availableIssueDate();

        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급기간과 발급수량이 유효할 경우 발급성공.")
    void issue_1() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        coupon.issue();

        Assertions.assertEquals(coupon.getIssuedQuantity(), 100);
    }

    @Test
    @DisplayName("발급수량이 초과 경우 예외발생.")
    void issue_2() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);

    }

    @Test
    @DisplayName("발급기간이 초과 경우 예외발생.")
    void issue_3() {
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_DATE);
    }
}