package com.distributedsystems.loadbalancer.helper.logger;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.distributedsystems.loadbalancer.service.LoadBalancerHandler.LoadBalancerHandler;

@Component
public class LoadBalancerLogger implements ILoadBalancerLogger {

    private static final Logger logger = Logger.getLogger(LoadBalancerHandler.class.getName());

    @Async
    @Override
    public CompletableFuture<Void> logInfo(String message) {
        logger.info(message);
        return CompletableFuture.completedFuture(null);
    }
    
}
