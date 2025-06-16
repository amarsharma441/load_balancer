package com.distributedsystems.loadbalancer.service.LoadBalancerHandler;

import com.distributedsystems.loadbalancer.model.Server;
import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

public interface ILoadBalancerHandler {

    CompletableFuture<ResponseEntity<String>> forwardRequest(HttpServletRequest request)  throws HttpStatusCodeException;

    void addServer(Server server) throws Exception ;

    void removeServer(Server server);

    void switchAlgorithm(String algorithmName);

    void performHealthCheck() throws Exception;
}

