package org.example.couponapi.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(value = JsonInclude.Include.NON_NULL)
public record CouponIssueResponseDto(boolean isSuccesss, String comment) {
}
