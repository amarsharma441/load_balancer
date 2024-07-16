package com.distributedsystems.loadbalancer.service.ServerHandler;

import java.util.List;

import com.distributedsystems.loadbalancer.model.Server;

public interface IServerHandler {

    public void addServer(Server server);

    public void removeServer(Server server);

    public List<Server> getServers();

}
