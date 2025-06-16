package com.distributedsystems.loadbalancer.service.LoadBalancerHandler;

import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.distributedsystems.loadbalancer.loadBalancingAlgorithm.ILoadBalancingAlgorithm;
import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadBalancerHandler implements ILoadBalancerHandler {
    private final ApplicationContext applicationContext;
    private final IServerHandler serverHandler;
    private final RestTemplate restTemplate;

    @Autowired
    @Qualifier("roundRobinBalancingAlgorithm")
    private ILoadBalancingAlgorithm loadBalancingAlgorithm;

    private static final Set<String> validAlgorithms = Set.of("random", "roundRobin", "leastConnections");

    @Override
    @Async
    public CompletableFuture<ResponseEntity<String>> forwardRequest(HttpServletRequest request) throws HttpStatusCodeException {
        Server server;
        try {
            server = this.loadBalancingAlgorithm.getServer();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        String backendServerUrl = server.getUrl();
        String url = backendServerUrl + request.getRequestURI();

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.add(headerName, request.getHeader(headerName));
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.info("Forwarding {} request to backend server URL: {}", method, url);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, entity, String.class);
        return CompletableFuture.completedFuture(responseEntity);
    }

    @Override
    public void addServer(Server server) throws Exception {
        serverHandler.addServer(server);
        performHealthCheck();
    }

    @Override
    public void removeServer(Server server) {
        serverHandler.removeServer(server);
    }

    @Override
    public void switchAlgorithm(String algorithmName) {
        if (validAlgorithms.contains(algorithmName)) {
            this.loadBalancingAlgorithm = (ILoadBalancingAlgorithm)
                    applicationContext.getBean(algorithmName + "BalancingAlgorithm");
        } else {
            throw new IllegalArgumentException("Invalid algorithm name: " + algorithmName);
        }
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    public void performHealthCheck() {
        log.info("Performing health check for servers");

        List<Server> allServers = serverHandler.getServers();

        if (allServers.isEmpty()) {
            log.info("No servers to perform health check");
            return;
        }

        for (Server server : allServers) {
            CompletableFuture<ResponseEntity<Boolean>> response = serverHandler.checkServerHealth(server);
            Boolean isHealthy = response.join().getBody();

            if (isHealthy == null || !isHealthy) {
                log.info("Server {} is unhealthy. Removing from the healthy servers list.", server.getUrl());
                try {
                    serverHandler.removeHealthyServer(server);
                    serverHandler.addUnhealthyServer(server);
                } catch (Exception e) {
                    log.info("Failed to remove server from healthy list", e);
                }
            } else {
                log.info("Server {} is healthy. Adding to the healthy servers list if not present.", server.getUrl());
                try {
                    serverHandler.addHealthyServer(server);
                    serverHandler.removeUnhealthyServer(server);
                } catch (Exception e) {
                    log.info("Failed to add server to healthy list", e);
                }
            }
        }
    }
}
