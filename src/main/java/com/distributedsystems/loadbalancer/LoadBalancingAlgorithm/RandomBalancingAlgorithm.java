package com.distributedsystems.loadbalancer.loadBalancingAlgorithm;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

@Component("randomBalancingAlgorithm")
public class RandomBalancingAlgorithm implements ILoadBalancingAlgorithm {

    private final IServerHandler serverHandler;

    private final Random random = new Random();

    public RandomBalancingAlgorithm(IServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @Override
    public Server getServer() throws Exception {
        List<Server> servers = serverHandler.getHealthyServers();
        if (servers.isEmpty()) {
            throw new Exception("No servers are healthy at a moment or no servers are registered");
        } 
        int index = random.nextInt(servers.size());
        return servers.get(index);
    }
}
