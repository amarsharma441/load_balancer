package com.distributedsystems.loadbalancer.service.ServerHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import com.distributedsystems.loadbalancer.model.Server;

@Service
public class ServerHandler implements IServerHandler {

    private List<Server> servers = new ArrayList<>();

    @Override
    public void addServer(Server server) {
        Predicate<Server> findServerByUrl = (s) -> s.getUrlWithPort().equals(server.getUrlWithPort());
        Server existingServer = servers.stream().filter(findServerByUrl).findFirst().orElse(null);
        if (existingServer == null)
            servers.add(server);
    }

    @Override
    public void removeServer(Server server) {
        Predicate<Server> findServerByUrl = (s) -> s.getUrlWithPort().equals(server.getUrlWithPort());
        servers.removeIf(findServerByUrl);
    }

    @Override
    public List<Server> getServers() {
        return servers;
    }
    
}
