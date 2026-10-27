package com.projects.distributed_lovable.account_service.dto.subscription;

public record CheckOutResponse(
        String stripeCheckOutUrl
) {
}
