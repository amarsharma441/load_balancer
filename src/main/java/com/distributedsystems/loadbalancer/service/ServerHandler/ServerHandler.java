package com.distributedsystems.loadbalancer.service.ServerHandler;

import java.util.HashSet;
import org.springframework.stereotype.Service;
import com.distributedsystems.loadbalancer.model.Server;

@Service
public class ServerHandler implements IServerHandler {

    private HashSet<Server> servers = new HashSet<>();

    @Override
    public void addServer(Server server) {
        throw new UnsupportedOperationException("Unimplemented method 'addServer'");
    }

    @Override
    public void removeServer(Server server) {
        throw new UnsupportedOperationException("Unimplemented method 'removeServer'");
    }

    @Override
    public HashSet<Server> getServers() {
        throw new UnsupportedOperationException("Unimplemented method 'getServers'");
    }
    
}
