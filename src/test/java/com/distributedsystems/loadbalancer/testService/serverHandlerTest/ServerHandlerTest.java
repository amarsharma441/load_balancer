package com.distributedsystems.loadbalancer.testService.ServerHandlerTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;import org.springframework.web.client.RestTemplate;

import com.distributedsystems.loadbalancer.model.Server;
import com.distributedsystems.loadbalancer.service.ServerHandler.ServerHandler;

public class ServerHandlerTest {

    @InjectMocks
    private ServerHandler serverHandler;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddServer() throws Exception {
        // Test adding a server
        Server server = new Server("http://localhost",8080);
        serverHandler.addServer(server);
        
        assertEquals(1, serverHandler.getServers().size());
        
        // Test adding a duplicate server
        assertThrows(Exception.class, () -> serverHandler.addServer(server));
    }

    @Test
    public void testRemoveServer() throws Exception {
        // Test removing a server
        Server server = new Server("http://localhost",8080);
        serverHandler.addServer(server);
        
        serverHandler.removeServer(server);
        assertEquals(0, serverHandler.getServers().size());
        
        // Test removing a non-existing server
        assertThrows(IllegalArgumentException.class, () -> serverHandler.removeServer(server));
    }

    @Test
    public void testCheckServerHealth() throws ExecutionException, InterruptedException {
        // Mocking successful health check response
        Server server = new Server("http://localhost",8080);
        String healthCheckUrl = server.getUrlWithPort() + "/health";
        ResponseEntity<Boolean> mockResponse = new ResponseEntity<>(true, HttpStatus.OK);
        when(restTemplate.getForEntity(healthCheckUrl, Boolean.class)).thenReturn(mockResponse);

        CompletableFuture<ResponseEntity<Boolean>> future = serverHandler.checkServerHealth(server);
        assertTrue(future.get().getBody());
        
        // Mocking failed health check response
        mockResponse = new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(healthCheckUrl, Boolean.class)).thenReturn(mockResponse);

        future = serverHandler.checkServerHealth(server);
        assertFalse(future.get().getBody());
    }

    @Test
    public void testAddUnhealthyServer() throws Exception {
        // Test adding an unhealthy server
        Server server = new Server("http://localhost",8080);
        serverHandler.addUnhealthyServer(server);
        
        assertEquals(1, serverHandler.getUnhealthyServers().size());
        
        // Test adding a duplicate unhealthy server
        assertThrows(Exception.class, () -> serverHandler.addUnhealthyServer(server));
    }

    @Test
    public void testRemoveUnhealthyServer() throws Exception {
        // Test removing an unhealthy server
        Server server = new Server("http://localhost",8080);
        serverHandler.addUnhealthyServer(server);
        
        serverHandler.removeUnhealthyServer(server);
        assertEquals(0, serverHandler.getUnhealthyServers().size());
        
        // Test removing a non-existing unhealthy server
        assertThrows(IllegalArgumentException.class, () -> serverHandler.removeUnhealthyServer(server));
    }

    @Test
    public void testAddHealthyServer() throws Exception {
        // Test adding a healthy server
        Server server = new Server("http://localhost",8080);
        serverHandler.addHealthyServer(server);
        
        assertEquals(1, serverHandler.getHealthyServers().size());
        
        // Test adding a duplicate healthy server
        assertThrows(Exception.class, () -> serverHandler.addHealthyServer(server));
    }

    @Test
    public void testRemoveHealthyServer() throws Exception {
        // Test removing a healthy server
        Server server = new Server("http://localhost",8080);
        serverHandler.addHealthyServer(server);
        
        serverHandler.removeHealthyServer(server);
        assertEquals(0, serverHandler.getHealthyServers().size());
        
        // Test removing a non-existing healthy server
        assertThrows(IllegalArgumentException.class, () -> serverHandler.removeHealthyServer(server));
    }
}
