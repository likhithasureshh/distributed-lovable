package com.projects.distributed_lovable.account_service.service;



import com.projects.distributed_lovable.account_service.dto.subscription.SubscriptionResponse;
import com.projects.distributed_lovable.common_lib.dto.PlanDto;
import com.projects.distributed_lovable.common_lib.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {
    SubscriptionResponse getMySubscription();


    void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId);

    void updateSubscription(String subscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String subscriptionId);

    void renewSubscriptionPeriod(String subId, Instant periodStart, Instant periodEnd);

    void makeSubscriptionPastDew(String subId);


    PlanDto getCurrentSubscribedPlanByUser();
}
