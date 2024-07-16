package com.distributedsystems.loadbalancer.service.LoadBalancerHandler;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.distributedsystems.loadbalancer.LoadBalancingAlgorithm.ILoadBalancingAlgorithm;
import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

import jakarta.servlet.http.HttpServletRequest;


@Service
public class LoadBalancerHandler implements ILoadBalancerHandler{

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IServerHandler serverHandler;

    @Autowired
    @Qualifier("roundRobinBalancingAlgorithm")
    private ILoadBalancingAlgorithm loadBalancingAlgorithm;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final Set<String> validAlgorithms;

    static {
        validAlgorithms = new HashSet<>();
        validAlgorithms.add("random");
        validAlgorithms.add("roundRobin");
    }

    @Override
    @Async
    public CompletableFuture<ResponseEntity<String>> forwardRequest(HttpServletRequest request) throws RestClientException {
        try {
            
            Server server = this.loadBalancingAlgorithm.getServer();

            String backendServerUrl = server.getUrlWithPort();
            String url = backendServerUrl + request.getRequestURI();

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.add(headerName, request.getHeader(headerName));
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, entity, String.class);
            return CompletableFuture.completedFuture(responseEntity);
        
        } catch (RestClientException e) {
            throw new RestClientException("Error forwarding request: " + e.getMessage(), e);
        } 
    }

    @Override
    public void addServer(Server server) {
        serverHandler.addServer(server);
    }

    @Override
    public void removeServer(Server server) {
        serverHandler.removeServer(server);
    }

    @Override
    public void switchAlgorithm(String algorithmName) {
        if (validAlgorithms.contains(algorithmName)) {
            this.loadBalancingAlgorithm = (ILoadBalancingAlgorithm) applicationContext.getBean(algorithmName + "BalancingAlgorithm");
        } else {
            throw new IllegalArgumentException("Invalid algorithm name: " + algorithmName);
        }
    }
    
}
