package com.distributedsystems.loadbalancer.service.LoadBalancerHandler;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.distributedsystems.loadbalancer.LoadBalancingAlgorithm.ILoadBalancingAlgorithm;
import com.distributedsystems.loadbalancer.helper.logger.ILoadBalancerLogger;
import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

import jakarta.servlet.http.HttpServletRequest;


@Service
public class LoadBalancerHandler implements ILoadBalancerHandler {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IServerHandler serverHandler;

    @Autowired
    @Qualifier("roundRobinBalancingAlgorithm")
    private ILoadBalancingAlgorithm loadBalancingAlgorithm;

    @Autowired
    private ILoadBalancerLogger loadBalancerLogger;

    @Autowired
    private RestTemplate restTemplate;

    private static final Set<String> validAlgorithms;

    static {
        validAlgorithms = new HashSet<>();
        validAlgorithms.add("random");
        validAlgorithms.add("roundRobin");
        validAlgorithms.add("leastConnections");
    }

    @Override
    @Async
    public CompletableFuture<ResponseEntity<String>> forwardRequest(HttpServletRequest request) throws HttpStatusCodeException {
        Server server = null;
        try {
            server = this.loadBalancingAlgorithm.getServer();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

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

        loadBalancerLogger.logInfo("LOG: Forwarding " + method + " request to backend server URL: " + url);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, entity, String.class);
        return CompletableFuture.completedFuture(responseEntity);
    }

    @Override
    public void addServer(Server server) throws Exception {
        serverHandler.addServer(server); 
        performHealthCheck();      
    }

    @Override
    public void removeServer(Server server) {
        serverHandler.removeServer(server);
    }

    @Override
    public void switchAlgorithm(String algorithmName) {
        if (validAlgorithms.contains(algorithmName)) {
            this.loadBalancingAlgorithm = (ILoadBalancingAlgorithm) applicationContext.getBean(algorithmName + "BalancingAlgorithm");
        } 
        else
            throw new IllegalArgumentException("Invalid algorithm name: " + algorithmName);
    }

    // check health of servers in every 1 min
    @Override
    @Scheduled(fixedDelay = 60000) 
    public void performHealthCheck() throws Exception {
        loadBalancerLogger.logInfo("LOG: Performing health check for servers");
        
        List<Server> allServers = serverHandler.getServers();
      
        if (allServers.size() == 0) {
            loadBalancerLogger.logInfo("LOG: No servers to perform health check");
            return;
        }
        
        for (Server server: allServers) {
            CompletableFuture<ResponseEntity<Boolean>> response = serverHandler.checkServerHealth(server);
            Boolean isHealthy = response.join().getBody();

            if (isHealthy == null || !isHealthy) {
                loadBalancerLogger.logInfo("Server " + server.getUrlWithPort() + " is unhealthy. Removing from the healthy servers list.");
                try {
                    serverHandler.removeHealthyServer(server);
                    serverHandler.addUnhealthyServer(server);
                } catch (Exception e) {
                    loadBalancerLogger.logInfo("Failed to remove");
                }
            } else {
                loadBalancerLogger.logInfo("Server " + server.getUrlWithPort() + " is healthy. Adding to the healthy servers list if not present.");
                try {
                    serverHandler.addHealthyServer(server);
                    serverHandler.removeUnhealthyServer(server);
                } catch (Exception e) {
                    loadBalancerLogger.logInfo("Failed to add");
                }
            }
        }
    }
    
}
