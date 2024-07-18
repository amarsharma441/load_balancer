package com.distributedsystems.loadbalancer.LoadBalancingAlgorithm;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

@Component("leastConnectionsBalancingAlgorithm")
public class LeastConnectionsAlgorithm implements ILoadBalancingAlgorithm {
    
    @Autowired 
    private IServerHandler serverHandler;

    private final Map<String, Integer> serverConnections = new ConcurrentHashMap<>();

    private static synchronized Server getServerWithLeastConnections(List<Server> servers, Map<String, Integer> serverConnections) {
        Server leastConnectedServer = null;
        int leastConnections = Integer.MAX_VALUE;

        for (Server server : servers) {
            String serverUrl = server.getUrlWithPort();
            int connections = serverConnections.getOrDefault(serverUrl, 0);

            if (connections < leastConnections) {
                leastConnections = connections;
                leastConnectedServer = server;
            }
        }

        if (leastConnectedServer != null) {
            serverConnections.put(leastConnectedServer.getUrlWithPort(), leastConnections + 1);
        }

        return leastConnectedServer;
    }

    @Override
    public Server getServer() throws Exception {
        List<Server> servers = serverHandler.getHealthyServers();
        if (servers.size() == 0) {
            throw new Exception("No servers are healthy at a moment or no servers are registered");
        }
        return getServerWithLeastConnections(servers, serverConnections);
    }
}
