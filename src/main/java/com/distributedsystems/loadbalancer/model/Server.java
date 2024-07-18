package com.distributedsystems.loadbalancer.model;

public class Server {

    private String url;
    private int portNumber;
    
    public Server(String url, int portNumber) {
        this.url = url;
        this.portNumber = portNumber;
    }

    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public int getPortNumber() {
        return portNumber;
    }
    
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getUrlWithPort() {
        return this.url + ":" + this.portNumber;
    }
}
