package com.distributedsystems.loadbalancer.testService.loadBalancerHandlerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.distributedsystems.loadbalancer.loadBalancingAlgorithm.ILoadBalancingAlgorithm;
import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.LoadBalancerHandler.LoadBalancerHandler;
import com.distributedsystems.loadbalancer.service.ServerHandler.IServerHandler;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class LoadBalancerHandlerTest {

    private LoadBalancerHandler loadBalancerHandler;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private IServerHandler serverHandler;

    @Mock
    private ILoadBalancingAlgorithm loadBalancingAlgorithm;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loadBalancerHandler = new LoadBalancerHandler(applicationContext, serverHandler, restTemplate);
        ReflectionTestUtils.setField(loadBalancerHandler, "loadBalancingAlgorithm", loadBalancingAlgorithm);
    }

    @Test
    void testForwardRequest() throws Exception {
        Server server = new Server("http://localhost:8081");
        when(loadBalancingAlgorithm.getServer()).thenReturn(server);
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("Test-Header")));
        when(request.getHeader("Test-Header")).thenReturn("Test-Value");

        ResponseEntity<String> mockResponse = new ResponseEntity<>("Response Body", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        CompletableFuture<ResponseEntity<String>> futureResponse = loadBalancerHandler.forwardRequest(request);
        ResponseEntity<String> response = futureResponse.join();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Response Body", response.getBody());
    }

    @Test
    void testAddServer() throws Exception {
        Server server = new Server("http://localhost:8081");
        doNothing().when(serverHandler).addServer(server);
        when(serverHandler.getServers()).thenReturn(Collections.singletonList(server));
        when(serverHandler.checkServerHealth(server))
                .thenReturn(CompletableFuture.completedFuture(new ResponseEntity<>(true, HttpStatus.OK)));

        loadBalancerHandler.addServer(server);

        verify(serverHandler).addServer(server);
        verify(serverHandler).checkServerHealth(server);
    }

    @Test
    void testRemoveServer() {
        Server server = new Server("http://localhost:8080");
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
        Server healthyServer = new Server("http://healthy-server:8080");
        Server unhealthyServer = new Server("http://unhealthy-server:8080");

        List<Server> servers = Arrays.asList(healthyServer, unhealthyServer);
        when(serverHandler.getServers()).thenReturn(servers);

        when(serverHandler.checkServerHealth(healthyServer))
                .thenReturn(CompletableFuture.completedFuture(new ResponseEntity<>(true, HttpStatus.OK)));
        when(serverHandler.checkServerHealth(unhealthyServer))
                .thenReturn(CompletableFuture.completedFuture(new ResponseEntity<>(false, HttpStatus.OK)));

        loadBalancerHandler.performHealthCheck();

        verify(serverHandler).addHealthyServer(healthyServer);
        verify(serverHandler).removeUnhealthyServer(healthyServer);

        verify(serverHandler).removeHealthyServer(unhealthyServer);
        verify(serverHandler).addUnhealthyServer(unhealthyServer);
    }
}