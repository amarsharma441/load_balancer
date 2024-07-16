package com.distributedsystems.loadbalancer.service.LoadBalancerHandler;

import com.distributedsystems.loadbalancer.model.Server;
import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

public interface ILoadBalancerHandler {

    public CompletableFuture<ResponseEntity<String>> forwardRequest(HttpServletRequest request)  throws RestClientException;

    public void addServer(Server server);

    public void removeServer(Server server);

    public void switchAlgorithm(String algorithmName);
}

