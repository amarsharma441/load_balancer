package com.distributedsystems.loadbalancer.loadBalancingAlgorithm;


import com.distributedsystems.loadbalancer.model.Server;

public interface ILoadBalancingAlgorithm {
    Server getServer() throws Exception;
}
