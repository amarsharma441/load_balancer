package com.distributedsystems.loadbalancer.loadBalancingAlgorithm;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

@Component("roundRobinBalancingAlgorithm")
public class RoundRobinBalancingAlgorithm implements ILoadBalancingAlgorithm {

    private final IServerHandler serverHandler;

    private static int currIndex = -1;

    public RoundRobinBalancingAlgorithm(IServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    private static synchronized void increaseIndex(int numberOfServers) {
        currIndex += 1;
        currIndex = currIndex % numberOfServers;
    }

    @Override
    public Server getServer() throws Exception {
        List<Server> servers = serverHandler.getHealthyServers();
        if (servers.isEmpty()) {
            throw new Exception("No servers are healthy at a moment or no servers are registered");
        }
        increaseIndex(servers.size());
        return servers.get(currIndex);
    }
}
