package com.distributedsystems.loadbalancer.service.serverHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.distributedsystems.loadbalancer.model.Server;

@Service
public class ServerHandler implements IServerHandler {

    private List<Server> servers = new ArrayList<>();
    private List<Server> unHealthyServers = new ArrayList<>();
    private List<Server> healthyServers = new ArrayList<>();
    
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void addServer(Server server) throws Exception {
        Predicate<Server> findServerByUrl = (s) -> s.getUrlWithPort().equals(server.getUrlWithPort());
        Server existingServer = servers.stream().filter(findServerByUrl).findFirst().orElse(null);
        if (existingServer == null)
            servers.add(server);
        else
            throw new Exception("Server already registered");
    }

    @Override
    public void removeServer(Server server) {
        Predicate<Server> findServerByUrl = (s) -> s.getUrlWithPort().equals(server.getUrlWithPort());
        if (!servers.removeIf(findServerByUrl))
            throw new IllegalArgumentException("Server Not Found: " + server.getUrlWithPort());
    }

    @Override
    public List<Server> getServers() {
        return servers;
    }

    @Async
    @Override
    public CompletableFuture<ResponseEntity<Boolean>> checkServerHealth(Server server) {
        String healthCheckUrl = server.getUrlWithPort() + "/health";
        try {
            ResponseEntity<Boolean> responseEntity = restTemplate.getForEntity(healthCheckUrl, Boolean.class);
            Boolean healthStatus = responseEntity.getStatusCode().is2xxSuccessful();
            return CompletableFuture.completedFuture(new ResponseEntity<>(healthStatus, responseEntity.getStatusCode()));
        } catch (RestClientException e) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public List<Server> getUnhealthyServers() {
        return unHealthyServers;
    }

    @Override
    public void addUnhealthyServer(Server server) throws Exception {
        Predicate<Server> findServerByUrl = (s) -> s.getUrlWithPort().equals(server.getUrlWithPort());
        Server existingunHealthyServer = unHealthyServers.stream().filter(findServerByUrl).findFirst().orElse(null);
        if (existingunHealthyServer == null)
            unHealthyServers.add(server);
        else
            throw new Exception("Server already marked as un healthy");
    }

    @Override
    public void removeUnhealthyServer(Server server) {
        Predicate<Server> findServerByUrl = (s) -> s.getUrlWithPort().equals(server.getUrlWithPort());
        if (!unHealthyServers.removeIf(findServerByUrl))
            throw new IllegalArgumentException("Server Not Found: " + server.getUrlWithPort());
    }

    @Override
    public List<Server> getHealthyServers() {
        return healthyServers;
    }

    @Override
    public void addHealthyServer(Server server) throws Exception {
        Predicate<Server> findServerByUrl = (s) -> s.getUrlWithPort().equals(server.getUrlWithPort());
        Server existingunHealthyServer = healthyServers.stream().filter(findServerByUrl).findFirst().orElse(null);
        if (existingunHealthyServer == null)
            healthyServers.add(server);
        else
            throw new Exception("Server already marked as healthy");
    }

    @Override
    public void removeHealthyServer(Server server) {
        Predicate<Server> findServerByUrl = (s) -> s.getUrlWithPort().equals(server.getUrlWithPort());
        if (!healthyServers.removeIf(findServerByUrl))
            throw new IllegalArgumentException("Server Not Found: " + server.getUrlWithPort());
    }
    
}
