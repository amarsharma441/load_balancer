package com.distributedsystems.loadbalancer.service.loadBalancerHandler;

import com.distributedsystems.loadbalancer.model.Server;
import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

public interface ILoadBalancerHandler {

    public CompletableFuture<ResponseEntity<String>> forwardRequest(HttpServletRequest request)  throws HttpStatusCodeException;

    public void addServer(Server server) throws Exception ;

    public void removeServer(Server server);

    public void switchAlgorithm(String algorithmName);

    public void performHealthCheck() throws Exception;
}

