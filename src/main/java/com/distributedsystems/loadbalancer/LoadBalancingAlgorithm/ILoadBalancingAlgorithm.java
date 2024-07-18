package com.distributedsystems.loadbalancer.LoadBalancingAlgorithm;


import com.distributedsystems.loadbalancer.model.Server;

public interface ILoadBalancingAlgorithm {
    public Server getServer() throws Exception;
}
