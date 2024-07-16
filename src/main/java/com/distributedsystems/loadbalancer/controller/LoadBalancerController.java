package com.distributedsystems.loadbalancer.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.LoadBalancerHandler.LoadBalancerHandler;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class LoadBalancerController {

    @Autowired
    private LoadBalancerHandler loadBalancerHandler;

    @RequestMapping(value = "/**")
    public ResponseEntity<String> handleRequest(HttpServletRequest request) throws IOException {
        return loadBalancerHandler.forwardRequest(request);
    }

    @PostMapping("/addServer")
    public void registerServer(@RequestBody Server serverUrl) {
        loadBalancerHandler.addServer(serverUrl);
    }

    @DeleteMapping("/removeServer")
    public void removeServer(@RequestBody Server serverUrl) {
        loadBalancerHandler.removeServer(serverUrl);
    }
}

