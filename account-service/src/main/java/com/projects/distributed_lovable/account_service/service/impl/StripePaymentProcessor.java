package com.projects.distributed_lovable.account_service.service.impl;

import com.projects.distributed_lovable.account_service.dto.subscription.CheckOutRequest;
import com.projects.distributed_lovable.account_service.dto.subscription.CheckOutResponse;
import com.projects.distributed_lovable.account_service.dto.subscription.PortalResponse;
import com.projects.distributed_lovable.account_service.entity.Plan;
import com.projects.distributed_lovable.account_service.entity.User;
import com.projects.distributed_lovable.account_service.repository.PlanRepository;
import com.projects.distributed_lovable.account_service.repository.UserRepository;
import com.projects.distributed_lovable.account_service.service.PaymentProcessor;
import com.projects.distributed_lovable.account_service.service.SubscriptionService;
import com.projects.distributed_lovable.common_lib.enums.SubscriptionStatus;
import com.projects.distributed_lovable.common_lib.errors.BadRequestException;
import com.projects.distributed_lovable.common_lib.errors.ResourceNotFoundException;
import com.projects.distributed_lovable.common_lib.security.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentProcessor implements PaymentProcessor {
    @Value("${client.url}")
    private String frontEndUrl;
    private final AuthUtil authUtil;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Override
    public CheckOutResponse createCheckOutUrl(CheckOutRequest request) {
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(()-> new ResourceNotFoundException("plan",request.planId().toString()));
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("user",userId.toString()));
        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(plan.getStripePriceId()).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(new SessionCreateParams.SubscriptionData.Builder()
                        .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                .build())
                        .build())
                .setSuccessUrl(frontEndUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontEndUrl+"/cancel.html")
                .putMetadata("planId",plan.getId().toString())
                .putMetadata("userId",userId.toString());
        try {
            String stripeCustomerId = user.getStripeCustomerId();
            if(stripeCustomerId==null || stripeCustomerId.isEmpty())
            {
                params.setCustomerEmail(user.getUsername());
            }
            else
            {
                params.setCustomer(stripeCustomerId);
            }
            Session session = Session.create(params.build());
            return new CheckOutResponse(session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PortalResponse getCustomerPortalUrl() {
        Long userId = authUtil.getCurrentUserId();
        User user = getUser(userId);
        String stripeCustomerId = user.getStripeCustomerId();
        if(stripeCustomerId == null || stripeCustomerId.isEmpty())
        {
            throw new BadRequestException("The user does not have " +
                    "the SubscriptionId  to open CustomerPortal "+stripeCustomerId);
        }
        try {
            com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(stripeCustomerId)
                            .setReturnUrl(frontEndUrl)
                            .build()
            );
            return new PortalResponse(session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.info("handle Event Type: {} ",type);
        switch(type)
        {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject,metadata);
            case "customer.subscription.updated" -> handleCustomerSubscriptionUpdated((Subscription) stripeObject);
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted((Subscription) stripeObject);
            case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject);
        }
    }

    private void handleCheckoutSessionCompleted(Session session,Map<String, String> metadata)
    {
        if(session == null)
        {
            log.error("session object is null in handleCheckoutSessionCompleted");
            return;
        }
        Long userId = Long.parseLong(metadata.get("userId"));
        Long planId = Long.parseLong(metadata.get("planId"));
        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();
        User user = getUser(userId);
        if(user != null)
        {
            user.setStripeCustomerId(customerId);
            userRepository.save(user);
        }
        subscriptionService.activateSubscription(userId,planId,subscriptionId,customerId);

    }
    private void handleCustomerSubscriptionUpdated(Subscription subscription)
    {
       if(subscription == null)
       {
           log.error("subscription object is null in handleCustomerSubscriptionUpdated");
           return;
       }
       SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
       if(status == null)
       {
           log.warn("Unknows {} status for subscription {}",subscription.getStatus(),subscription.getId());
           return;
       }
        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant periodStart = toInstant(item.getCurrentPeriodStart());
        Instant periodEnd = toInstant(item.getCurrentPeriodEnd());
        Long planId = resolvePlanId(item.getPrice());
        subscriptionService.updateSubscription(
                subscription.getId(),status,periodStart,periodEnd
                ,subscription.getCancelAtPeriodEnd(),planId
        );

    }




    private void handleCustomerSubscriptionDeleted(Subscription subscription)
    {
        if(subscription == null)
        {
            log.error("subscription object is null in handleCustomerSubscriptionDeleted");
            return;
        }
        subscriptionService.cancelSubscription(subscription.getId());
    }

    private void handleInvoicePaid(Invoice invoice)
    {
         String subId = extractSubscriptionFromInvoice(invoice);
         if(subId == null)
         {
             log.error("subId is null");
             return;
         }
        try {
            Subscription subscription = Subscription.retrieve(subId);
            SubscriptionItem item = subscription.getItems().getData().get(0);
            Instant periodStart = toInstant(item.getCurrentPeriodStart());
            Instant periodEnd = toInstant(item.getCurrentPeriodEnd());
            subscriptionService.renewSubscriptionPeriod(subId,periodStart,periodEnd);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }



    private void handleInvoicePaymentFailed(Invoice invoice)
    {
         String subId = extractSubscriptionFromInvoice(invoice);
         if(subId == null)
         {
             log.error("subId is null inside in handleInvoicePaymentFailed ");
             return;
         }
         subscriptionService.makeSubscriptionPastDew(subId);

    }
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", userId.toString()));
    }

    private Instant toInstant(Long epoch) {
         return epoch!=null? Instant.ofEpochSecond(epoch):null;
    }

    private Long resolvePlanId(Price price) {
        if(price == null)
        {
            log.error("price is null");
            return null;
        }
        return planRepository.findByStripePriceId(price.getId())
                .map(Plan::getId)
                .orElseThrow(()-> new ResourceNotFoundException("planId",price.toString()));
    }
    private SubscriptionStatus mapStripeStatusToEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status: {}", status);
                yield null;
            }
        };
    }

    private String extractSubscriptionFromInvoice(Invoice invoice) {
        var parent = invoice.getParent();
        if(parent == null)
        {
            log.error("parent is null");
            return null;
        }
        var subDetails = parent.getSubscriptionDetails();
        if(subDetails == null)
        {
            log.error("subDetails is null");
            return null;
        }
        return subDetails.getSubscription();
    }


}
