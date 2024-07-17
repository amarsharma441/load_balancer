package com.distributedsystems.loadbalancer.loadBalancingAlgorithm;

import com.distributedsystems.loadbalancer.model.Server;

public interface ILoadBalancingAlgorithm {
    public Server getServer() throws Exception;
}
