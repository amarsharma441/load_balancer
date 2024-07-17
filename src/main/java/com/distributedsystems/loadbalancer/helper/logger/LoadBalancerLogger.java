package com.distributedsystems.loadbalancer.helper.logger;

import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.distributedsystems.loadbalancer.service.LoadBalancerHandler.LoadBalancerHandler;

@Component
public class LoadBalancerLogger implements ILoadBalancerLogger {

    private static final Logger logger = Logger.getLogger(LoadBalancerHandler.class.getName());

    @Override
    public void logInfo(String message) {
        logger.info(message);
    }
    
}
