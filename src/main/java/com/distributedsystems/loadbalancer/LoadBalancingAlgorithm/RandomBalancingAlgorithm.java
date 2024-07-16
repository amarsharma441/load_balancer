package com.distributedsystems.loadbalancer.LoadBalancingAlgorithm;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

@Component("randomBalancingAlgorithm")
public class RandomBalancingAlgorithm implements ILoadBalancingAlgorithm {

    
    @Autowired 
    private IServerHandler serverHandler;

    private final Random random = new Random();

    @Override
    public Server getServer() {
        List<Server> servers = serverHandler.getServers(); 
        int index = random.nextInt(servers.size());
        return servers.get(index);
    }
}
