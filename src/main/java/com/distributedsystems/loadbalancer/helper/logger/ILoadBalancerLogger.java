package com.distributedsystems.loadbalancer.helper.logger;

import java.util.concurrent.CompletableFuture;

public interface ILoadBalancerLogger {
    public CompletableFuture<Void> logInfo(String message);
}
