package com.distributedsystems.loadbalancer.service.serverHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;

import com.distributedsystems.loadbalancer.model.Server;

public interface IServerHandler {

    public void addServer(Server server) throws Exception ;

    public void removeServer(Server server);

    public List<Server> getServers();

    public CompletableFuture<ResponseEntity<Boolean>> checkServerHealth(Server server);

    public List<Server> getUnhealthyServers();

    public void addUnhealthyServer(Server server) throws Exception;

    public void removeUnhealthyServer(Server server);

    public List<Server> getHealthyServers();

    public void addHealthyServer(Server server) throws Exception;

    public void removeHealthyServer(Server server);

}
