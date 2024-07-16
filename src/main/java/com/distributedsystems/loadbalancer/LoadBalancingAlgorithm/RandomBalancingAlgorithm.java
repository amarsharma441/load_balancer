package com.distributedsystems.loadbalancer.LoadBalancingAlgorithm;

import org.springframework.stereotype.Component;

@Component
public class RandomBalancingAlgorithm implements ILoadBalancingAlgorithm {

    @Override
    public String getServer() {
        throw new UnsupportedOperationException("Unimplemented method 'getServer'");
    }
    
}
