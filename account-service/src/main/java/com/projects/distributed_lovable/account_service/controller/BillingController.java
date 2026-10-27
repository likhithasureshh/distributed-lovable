package com.projects.distributed_lovable.account_service.controller;


import com.projects.distributed_lovable.account_service.dto.subscription.CheckOutRequest;
import com.projects.distributed_lovable.account_service.dto.subscription.CheckOutResponse;
import com.projects.distributed_lovable.account_service.dto.subscription.PortalResponse;
import com.projects.distributed_lovable.account_service.dto.subscription.SubscriptionResponse;
import com.projects.distributed_lovable.account_service.service.PaymentProcessor;
import com.projects.distributed_lovable.account_service.service.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@Slf4j
public class BillingController {
    private final SubscriptionService subscriptionService;
    private final PaymentProcessor paymentProcessor;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;


    @GetMapping("/api/me/subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription()
    {
        return ResponseEntity.ok(subscriptionService.getMySubscription());
    }

    @PostMapping("/api/payments/checkout")
    public ResponseEntity<CheckOutResponse> checkOut(@RequestBody CheckOutRequest request)
    {
        return ResponseEntity.ok(paymentProcessor.createCheckOutUrl(request));
    }

    @PostMapping("/api/payments/portal")
    public ResponseEntity<PortalResponse> getCustomerPortalUrl()
    {
        return ResponseEntity.ok(paymentProcessor.getCustomerPortalUrl());
    }

    @PostMapping("webhooks/payment")
    public ResponseEntity<String> handelWebhookEvents(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signHeader
    )
    {
        try {
            Event event = Webhook.constructEvent(payload,signHeader,webhookSecret);
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;

            if (deserializer.getObject().isPresent()) { // happy case
                stripeObject = deserializer.getObject().get();
            } else {
                // Fallback: Deserialize from raw JSON
                try {
                    stripeObject = deserializer.deserializeUnsafe();
                    if (stripeObject == null) {
                        log.warn("Failed to deserialize webhook object for event: {}", event.getType());
                        return ResponseEntity.ok().build();
                    }
                } catch (Exception e) {
                    log.error("Unsafe deserialization failed for event {}: {}", event.getType(), e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Deserialization failed");
                }
            }

            // Now extract metadata only if it's a Checkout Session
            Map<String, String> metadata = new HashMap<>();
            if (stripeObject instanceof Session session) {
                metadata = session.getMetadata();
            }

            // Pass to your processor
            paymentProcessor.handleWebhookEvent(event.getType(), stripeObject, metadata);
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            throw new RuntimeException(e);
        }

    }

}
