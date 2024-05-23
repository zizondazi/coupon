package org.example.couponapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.couponapi.controller.dto.CouponIssueRequestDto;
import org.example.couponcore.service.CouponIssueService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;

    public void issueRequestV1(CouponIssueRequestDto coupon) {
        couponIssueService.issue(coupon.couponId(), coupon.userId());
        log.info("쿠폰 발급 완료. couponId : %s, userID : %s".formatted(coupon.couponId(), coupon.userId()));
    }
}
