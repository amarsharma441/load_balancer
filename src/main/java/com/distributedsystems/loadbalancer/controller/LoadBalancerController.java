package com.distributedsystems.loadbalancer.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import com.distributedsystems.loadbalancer.helper.logger.ILoadBalancerLogger;
import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.loadBalancerHandler.ILoadBalancerHandler;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class LoadBalancerController {

    @Autowired
    private ILoadBalancerHandler loadBalancerHandler;

    @Autowired
    private ILoadBalancerLogger loadBalancerLogger;

    @RequestMapping(value = "/**")
    public CompletableFuture<ResponseEntity<String>> handleRequest(HttpServletRequest request) throws IOException {
        try {
            CompletableFuture<ResponseEntity<String>> response = loadBalancerHandler.forwardRequest(request);
            loadBalancerLogger.logInfo("LOG: Request success: " + HttpStatus.OK);
            return response;
        } catch (ResponseStatusException e) {
            loadBalancerLogger.logInfo("LOG: Request failed: " + e.getStatusCode() + e.getMessage());
            return CompletableFuture.completedFuture(
                ResponseEntity.status(e.getStatusCode()).body(e.getMessage())
            );
        } catch (HttpStatusCodeException e) {
            loadBalancerLogger.logInfo("LOG: Request failed: " + e.getStatusCode() + e.getResponseBodyAsString());
            return CompletableFuture.completedFuture(
                ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            loadBalancerLogger.logInfo("LOG: Request failed: " + HttpStatus.INTERNAL_SERVER_ERROR + e.getMessage());
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())
            );
        }
    }


    @PostMapping("/addServer")
    public ResponseEntity<String> addServer(@RequestBody Server server) {
        try {
            loadBalancerHandler.addServer(server);
            loadBalancerLogger.logInfo("LOG: Server Added: " + server.getUrlWithPort());
            return ResponseEntity.ok("Server Added: " + server.getUrlWithPort());
        } catch (Exception e) {
            loadBalancerLogger.logInfo("LOG: Couldn't add server: " + server.getUrlWithPort() + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        
    }

    @DeleteMapping("/removeServer")
    public ResponseEntity<String> removeServer(@RequestBody Server server) {
        try {
            loadBalancerHandler.removeServer(server);
            loadBalancerLogger.logInfo("LOG: Server Removed: " + server.getUrlWithPort());
            return ResponseEntity.ok("Server Removed: " + server.getUrlWithPort());
        } catch (Exception e) {
            loadBalancerLogger.logInfo("LOG: Couldn't remove server: " + server.getUrlWithPort() + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/switchAlgo")
    public ResponseEntity<String> switchAlgorithm(@RequestBody String algorithmName) {
        try {
            loadBalancerHandler.switchAlgorithm(algorithmName);
            loadBalancerLogger.logInfo("LOG: Algorithm switched to: " + algorithmName);
            return ResponseEntity.ok("Algorithm switched to: " + algorithmName);
        } catch (IllegalArgumentException e) {
            loadBalancerLogger.logInfo("LOG: Couldn't change algo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //to suppress 404 for favicon.ico - web browsers automatically request the favicon.ico file when loading a web page
    @RequestMapping("/favicon.ico")
    public void favicon() {
        return;
    }

}

