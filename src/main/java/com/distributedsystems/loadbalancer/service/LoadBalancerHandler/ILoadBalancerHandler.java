package com.distributedsystems.loadbalancer.service.LoadBalancerHandler;

import com.distributedsystems.loadbalancer.model.Server;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ILoadBalancerHandler {

    public ResponseEntity<String> forwardRequest(HttpServletRequest request);

    public void addServer(Server server);

    public void removeServer(Server server);
}

