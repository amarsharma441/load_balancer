package com.distributedsystems.loadbalancer.testService.LoadBalancerHandlerTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.distributedsystems.loadbalancer.LoadBalancingAlgorithm.ILoadBalancingAlgorithm;
import com.distributedsystems.loadbalancer.helper.logger.ILoadBalancerLogger;
import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.LoadBalancerHandler.LoadBalancerHandler;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

import jakarta.servlet.http.HttpServletRequest;

class LoadBalancerHandlerTest {

    @InjectMocks
    private LoadBalancerHandler loadBalancerHandler;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private IServerHandler serverHandler;

    @Mock
    @Qualifier("roundRobinBalancingAlgorithm")
    private ILoadBalancingAlgorithm loadBalancingAlgorithm;

    @Mock
    private ILoadBalancerLogger loadBalancerLogger;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testForwardRequest() throws Exception {
        Server server = new Server("http://localhost", 8081);
        when(loadBalancingAlgorithm.getServer()).thenReturn(server);
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Test-Header", "Test-Value");

        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("Test-Header")));
        when(request.getHeader(anyString())).thenReturn("Test-Value");

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Response Body", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        CompletableFuture<ResponseEntity<String>> responseFuture = loadBalancerHandler.forwardRequest(request);
        ResponseEntity<String> response = responseFuture.join();

        verify(loadBalancerLogger).logInfo("LOG: Forwarding GET request to backend server URL: " + server.getUrlWithPort() + "/test");
        assert response.getBody().equals("Response Body");
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testAddServer() throws Exception {
        Server server = new Server("http://localhost", 8081);
        doNothing().when(serverHandler).addServer(server);

        loadBalancerHandler.addServer(server);

        verify(serverHandler).addServer(server);
        verify(loadBalancerLogger).logInfo("LOG: Performing health check for servers");
    }

    @Test
    void testRemoveServer() {
        Server server = new Server("http://localhost", 8080);

        loadBalancerHandler.removeServer(server);

        verify(serverHandler).removeServer(server);
    }

    @Test
    void testSwitchAlgorithm() {
        when(applicationContext.getBean("randomBalancingAlgorithm")).thenReturn(loadBalancingAlgorithm);
        loadBalancerHandler.switchAlgorithm("random");

        verify(applicationContext).getBean("randomBalancingAlgorithm");
    }

    @Test
    void testPerformHealthCheck() throws Exception {
        Server healthyServer = new Server("http://healthy-server", 8080);
        Server unhealthyServer = new Server("http://unhealthy-server", 8080);

        List<Server> allServers = Arrays.asList(healthyServer, unhealthyServer);
        when(serverHandler.getServers()).thenReturn(allServers);

        CompletableFuture<ResponseEntity<Boolean>> healthyResponse = CompletableFuture.completedFuture(new ResponseEntity<>(true, HttpStatus.OK));
        CompletableFuture<ResponseEntity<Boolean>> unhealthyResponse = CompletableFuture.completedFuture(new ResponseEntity<>(false, HttpStatus.OK));

        when(serverHandler.checkServerHealth(healthyServer)).thenReturn(healthyResponse);
        when(serverHandler.checkServerHealth(unhealthyServer)).thenReturn(unhealthyResponse);

        loadBalancerHandler.performHealthCheck();

        verify(loadBalancerLogger).logInfo("LOG: Performing health check for servers");
        verify(serverHandler).addHealthyServer(healthyServer);
        verify(serverHandler).removeUnhealthyServer(healthyServer);

        verify(loadBalancerLogger).logInfo("Server http://unhealthy-server:8080 is unhealthy. Removing from the healthy servers list.");
        verify(serverHandler).removeHealthyServer(unhealthyServer);
        verify(serverHandler).addUnhealthyServer(unhealthyServer);
    }
}
