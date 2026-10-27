package com.projects.distributed_lovable.account_service.service;

import com.projects.distributed_lovable.account_service.dto.subscription.CheckOutRequest;
import com.projects.distributed_lovable.account_service.dto.subscription.CheckOutResponse;
import com.projects.distributed_lovable.account_service.dto.subscription.PortalResponse;
import com.stripe.model.StripeObject;

import java.util.Map;

public interface PaymentProcessor {
    CheckOutResponse createCheckOutUrl(CheckOutRequest request);

    PortalResponse getCustomerPortalUrl();

    void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata);
}
