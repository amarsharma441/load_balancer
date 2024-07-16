package com.distributedsystems.loadbalancer.service.LoadBalancerHandler;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.distributedsystems.loadbalancer.model.Server;

import jakarta.servlet.http.HttpServletRequest;


@Service
public class LoadBalancerHandler implements ILoadBalancerHandler{

    @Override
    public ResponseEntity<String> forwardRequest(HttpServletRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'forwardRequest'");
    }

    @Override
    public void addServer(Server server) {
        throw new UnsupportedOperationException("Unimplemented method 'addServer'");
    }

    @Override
    public void removeServer(Server server) {
        throw new UnsupportedOperationException("Unimplemented method 'removeServer'");
    }
    
}
