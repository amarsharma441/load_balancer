package com.distributedsystems.loadbalancer.service.ServerHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.distributedsystems.loadbalancer.model.Server;

@Slf4j
@Service
public class ServerHandler implements IServerHandler {

    private final List<Server> servers = new ArrayList<>();
    private final List<Server> unHealthyServers = new ArrayList<>();
    private final List<Server> healthyServers = new ArrayList<>();
    private final RestTemplate restTemplate;

    public ServerHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void addServer(Server server) {
        Predicate<Server> matchByUrl = s -> s.getUrl().equals(server.getUrl());
        if (servers.stream().noneMatch(matchByUrl)) {
            servers.add(server);
            log.info("Added new server: {}", server.getUrl());
        } else {
            log.warn("Attempt to add duplicate server: {}", server.getUrl());
        }
    }

    @Override
    public void removeServer(Server server) {
        Predicate<Server> matchByUrl = s -> s.getUrl().equals(server.getUrl());
        if (servers.removeIf(matchByUrl)) {
            log.info("Removed server: {}", server.getUrl());
        } else {
            log.warn("Server not found for removal: {}", server.getUrl());
        }
        removeUnhealthyServer(server);
        removeHealthyServer(server);
    }

    @Override
    public List<Server> getServers() {
        return servers;
    }

    @Async
    @Override
    public CompletableFuture<ResponseEntity<Boolean>> checkServerHealth(Server server) {
        String healthCheckUrl = server.getUrl() + "/health";
        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(healthCheckUrl, Boolean.class);
            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            log.info("Health check for {} returned: {}", server.getUrl(), isHealthy);
            return CompletableFuture.completedFuture(new ResponseEntity<>(isHealthy, response.getStatusCode()));
        } catch (RestClientException e) {
            log.error("Health check failed for {}: {}", server.getUrl(), e.getMessage());
            return CompletableFuture.completedFuture(new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public List<Server> getUnhealthyServers() {
        return unHealthyServers;
    }

    @Override
    public void addUnhealthyServer(Server server) {
        Predicate<Server> matchByUrl = s -> s.getUrl().equals(server.getUrl());
        if (unHealthyServers.stream().noneMatch(matchByUrl)) {
            unHealthyServers.add(server);
            log.info("Marked server as unhealthy: {}", server.getUrl());
        } else {
            log.warn("Server already marked as unhealthy: {}", server.getUrl());
        }
    }

    @Override
    public void removeUnhealthyServer(Server server) {
        Predicate<Server> matchByUrl = s -> s.getUrl().equals(server.getUrl());
        if (unHealthyServers.removeIf(matchByUrl)) {
            log.info("Removed server from unhealthy list: {}", server.getUrl());
        } else {
            log.warn("Unhealthy server not found for removal: {}", server.getUrl());
        }
    }

    @Override
    public List<Server> getHealthyServers() {
        return healthyServers;
    }

    @Override
    public void addHealthyServer(Server server) {
        Predicate<Server> matchByUrl = s -> s.getUrl().equals(server.getUrl());
        if (healthyServers.stream().noneMatch(matchByUrl)) {
            healthyServers.add(server);
            log.info("Marked server as healthy: {}", server.getUrl());
        } else {
            log.warn("Server already marked as healthy: {}", server.getUrl());
        }
    }

    @Override
    public void removeHealthyServer(Server server) {
        Predicate<Server> matchByUrl = s -> s.getUrl().equals(server.getUrl());
        if (healthyServers.removeIf(matchByUrl)) {
            log.info("Removed server from healthy list: {}", server.getUrl());
        } else {
            log.warn("Healthy server not found for removal: {}", server.getUrl());
        }
    }
}