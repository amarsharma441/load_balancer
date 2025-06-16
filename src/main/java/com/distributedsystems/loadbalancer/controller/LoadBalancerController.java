package com.distributedsystems.loadbalancer.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.LoadBalancerHandler.ILoadBalancerHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/v1")
public class LoadBalancerController {

    private ILoadBalancerHandler loadBalancerHandler;

    @RequestMapping(value = "/**")
    public CompletableFuture<ResponseEntity<String>> handleRequest(HttpServletRequest request) {
        try {
            CompletableFuture<ResponseEntity<String>> response = loadBalancerHandler.forwardRequest(request);
            log.info("LOG: Request success: {}", HttpStatus.OK);
            return response;
        } catch (ResponseStatusException e) {
            log.info("LOG: Request failed : {}", e.getStatusCode() + e.getMessage());
            return CompletableFuture.completedFuture(
                ResponseEntity.status(e.getStatusCode()).body(e.getMessage())
            );
        } catch (HttpStatusCodeException e) {
            log.info("LOG: Request failed: {}", e.getStatusCode() + e.getResponseBodyAsString());
            return CompletableFuture.completedFuture(
                ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            log.info("LOG : Request failed: {}", HttpStatus.INTERNAL_SERVER_ERROR + e.getMessage());
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())
            );
        }
    }


    @PostMapping("/addServer")
    public ResponseEntity<String> addServer(@RequestBody Server server) {
        try {
            loadBalancerHandler.addServer(server);
            log.info("LOG: Server Added: {}", server.getUrl());
            return ResponseEntity.ok("Server Added: " + server.getUrl());
        } catch (Exception e) {
            log.info("LOG: Couldn't add server: {}", server.getUrl() + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        
    }

    @DeleteMapping("/removeServer")
    public ResponseEntity<String> removeServer(@RequestBody Server server) {
        try {
            loadBalancerHandler.removeServer(server);
            log.info("LOG: Server Removed: {}", server.getUrl());
            return ResponseEntity.ok("Server Removed: " + server.getUrl());
        } catch (Exception e) {
            log.info("LOG: Couldn't remove server: {}", server.getUrl() + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/switchAlgo")
    public ResponseEntity<String> switchAlgorithm(@RequestBody String algorithmName) {
        try {
            loadBalancerHandler.switchAlgorithm(algorithmName);
            log.info("LOG: Algorithm switched to: {}", algorithmName);
            return ResponseEntity.ok("Algorithm switched to: " + algorithmName);
        } catch (IllegalArgumentException e) {
            log.info("LOG: Couldn't change algo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //to suppress 404 for favicon.ico - web browsers automatically request the favicon.ico file when loading a web page
    @RequestMapping("/favicon.ico")
    public void favicon() {}

}

