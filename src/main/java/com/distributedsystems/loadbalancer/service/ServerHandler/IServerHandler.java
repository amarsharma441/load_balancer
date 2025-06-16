package com.distributedsystems.loadbalancer.service.ServerHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;

import com.distributedsystems.loadbalancer.model.Server;

public interface IServerHandler {

    void addServer(Server server) throws Exception ;

    void removeServer(Server server);

    List<Server> getServers();

    CompletableFuture<ResponseEntity<Boolean>> checkServerHealth(Server server);

    List<Server> getUnhealthyServers();

    void addUnhealthyServer(Server server) throws Exception;

    void removeUnhealthyServer(Server server);

    List<Server> getHealthyServers();

    void addHealthyServer(Server server) throws Exception;

    void removeHealthyServer(Server server);

}
