package com.distributedsystems.loadbalancer.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.LoadBalancerHandler.ILoadBalancerHandler;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class LoadBalancerController {

    @Autowired
    private ILoadBalancerHandler loadBalancerHandler;

    @RequestMapping(value = "/**")
    public CompletableFuture<ResponseEntity<String>>  handleRequest(HttpServletRequest request) throws IOException {
        return loadBalancerHandler.forwardRequest(request);
    }

    @PostMapping("/addServer")
    public void addServer(@RequestBody Server server) {
        loadBalancerHandler.addServer(server);
    }

    @DeleteMapping("/removeServer")
    public void removeServer(@RequestBody Server server) {
        loadBalancerHandler.removeServer(server);
    }

    @PutMapping("/switchAlgo")
    public void switchAlgorithm(@RequestBody String algorithmName) {
        loadBalancerHandler.switchAlgorithm(algorithmName);
    }

    //to suppress 404 for favicon.ico - web browsers automatically request the favicon.ico file when loading a web page
    @RequestMapping("/favicon.ico")
    public void favicon() {
        return;
    }

}

