package com.distributedsystems.loadbalancer.service.ServerHandler;

import java.util.HashSet;
import com.distributedsystems.loadbalancer.model.Server;

public interface IServerHandler {

    public void addServer(Server server);

    public void removeServer(Server server);

    public HashSet<Server> getServers();

}
