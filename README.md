# Load Balancer

This load balancer is designed to distribute incoming HTTP requests across multiple backend servers efficiently. It supports multiple load balancing algorithms (round-robin, random, least connections), dynamic registration/removal of servers, and robust error handling.

## Architecture

### Components

1. **Controller**:
   - `LoadBalancerController`: Handles incoming HTTP requests and interacts with the service layer to route these requests to backend servers.

2. **Service**:
   - `LoadBalancerHandler`: Implements the core load balancing logic, selecting the appropriate backend server based on the chosen algorithm.
   - `ServerHandler`: Manages the registration, removal, and monitoring of backend servers.

3. **Load Balancing Algorithms**:
   - `RoundRobinBalancingAlgorithm`: Implements the round-robin load balancing algorithm.
   - `RandomBalancingAlgorithm`: Implements a random selection algorithm.
   - `LeastConnectionsAlgorithm`: Selects the server with the least number of active connections.

4. **Helper/Logger**:
   - `LoadBalancerLogger`: Provides logging functionality to monitor and debug the system.

5. **Model**:
   - `Server`: Represents a backend server with its properties (e.g., url, port).

6. **Config**:
   - `AppConfig`: Contains application configuration settings.

### Data Flow

* An incoming HTTP request is received by the `LoadBalancerController`.
* The controller forwards the request to the `LoadBalancerHandler`.
* The `LoadBalancerHandler` uses the selected load balancing algorithm to choose a backend server.
* The request is routed to the chosen backend server.
* Responses from the backend server are passed back to the client.

## Class Descriptions

1. **LoadBalancerController**
   - `handleRequest(HttpServletRequest request)`: Receives an HTTP request from the client, forwards it to `LoadBalancerHandler`, and returns the response to the client.

2. **LoadBalancerHandler**
   - `routeRequest(HttpServletRequest request)`: Uses the configured load balancing algorithm to select a server, routes the request to the selected server, and returns the server's response to the controller.
   - `addServer(Server server)`: Registers a new server to the load balancer by utilizing ServerHandler service.
   - `removeServer(Server server)`: Deregisters an existing server from the load balancer  by utilizing ServerHandler service.
   - `switchAlgorithm(String algorithmName)`: Switches the load balancing algorithm to the specified algorithm.
   - `performHealthCheck()`: Performs periodic health checks on all registered servers and updates their status by utilizing ServerHandler service.
2. **ServerHandler**
   - `addServer(Server server)`: Registers a new server to the load balancer. Throws an exception if the server is already registered.
   - `removeServer(Server server)`: Deregisters an existing server from the load balancer. Throws an exception if the server is not found.
   - `getServers()`: Returns the list of all registered servers.
   - `checkServerHealth(Server server)`: Asynchronously checks the health of a specified server by querying its `/health` endpoint. Returns a `CompletableFuture` containing the health status.
   - `getUnhealthyServers()`: Returns the list of unhealthy servers.
   - `addUnhealthyServer(Server server)`: Adds a server to the list of unhealthy servers. Throws an exception if the server is already marked as unhealthy.
   - `removeUnhealthyServer(Server server)`: Removes a server from the list of unhealthy servers. Throws an exception if the server is not found.
   - `getHealthyServers()`: Returns the list of healthy servers.
   - `addHealthyServer(Server server)`: Adds a server to the list of healthy servers. Throws an exception if the server is already marked as healthy.
   - `removeHealthyServer(Server server)`: Removes a server from the list of healthy servers. Throws an exception if the server is not found.

4. **RandomBalancingAlgorithm**
   - `getServer()`: Retrieves the list of healthy servers from `ServerHandler`. If no servers are healthy or registered, it throws an exception. Otherwise, it randomly selects and returns a server from the list of healthy servers.

5. **RandomBalancingAlgorithm**
   - `getServer()`: Retrieves the list of healthy servers from `ServerHandler`. If no servers are healthy or registered, it throws an exception. Otherwise, it randomly selects and returns a server from the list of healthy servers.

6. **LeastConnectionsAlgorithm**
   - `getServer()`: Retrieves the list of healthy servers from `ServerHandler`. If no servers are healthy or registered, it throws an exception. Otherwise, it selects and returns the server with the least number of active connections.

7. **LoadBalancerLogger**
   - `logInfo(String message)`: Implements logging functionality to record system activities.

8. **Server**
    - `Server(String url, int portNumber)`: Constructor to initialize a server with the specified URL and port number.
    - `getUrl()`: Returns the URL of the server.
    - `setUrl(String url)`: Sets the URL of the server.
    - `getPortNumber()`: Returns the port number of the server.
    - `setPortNumber(int portNumber)`: Sets the port number of the server.
    - `getUrlWithPort()`: Returns the URL of the server concatenated with the port number.

9. **AppConfig**
    - Configuration class for Spring Beans.
    - `restTemplate()`: Bean definition for `RestTemplate`, which is used for making HTTP requests.


## Steps to Run the Project

1. **Checkout the load_balancer project repository**.
2. **Requirements**:
   - Java 11 or above
   - Spring Boot 3.3.1 or above
   - Maven 3.9.8 or above
3. **Build and Run the Load Balancer**:
   - Navigate to the load_balancer project directory.
   - Run the following commands:
     ```bash
     mvn clean
     mvn package
     java -jar target/load-balancer.jar --server.port=8181
     ```
   - The load balancer will run on port 8181 by default. If you wish to run on different, you can mention in the above command.
4. **Start Mock-Backend Servers**:
   - Navigate to the load_balancer project directory.
   - Run the following commands each in a separate terminal:
     ```bash
     java -jar mock-backend-server/mock-server.jar --server.port=8081
     java -jar mock-backend-server/mock-server.jar --server.port=8082
     java -jar mock-backend-server/mock-server.jar --server.port=8083
     ```
   - You can start more mock backend servers on different ports.

   **Note**: This mock server has two endpoints:
   - `GET: /api/data`: Returns mock data along with the port number on which the server is listening.
   - `GET: /health`: Returns 200 if the server is healthy.
5. **Handling No Registered Servers**:
   - If you make a request to the load balancer without registering any servers, it will return:
     ```
     404 NOT_FOUND - No servers are healthy at the moment or no servers are registered
     ```
     Also shown in image below:
     ![](https://github.com/amarsharma441/load_balancer/tree/main/src/main/resources/static/no_server_registered.png)
     
6. **Registering and Deregistering Servers**:
   - **Register a Server**:
     ```bash
     POST /addServer
     Body:
     {
       "url": "http://localhost",
       "portNumber": 8081
     }
     ```
   - **Deregister a Server**:
     ```bash
     DELETE /removeServer
     Body:
     {
       "url": "http://localhost",
       "portNumber": 8081
     }
     ```
7. **Health Checks**:
   - The load balancer periodically (set to 1 minute) checks the health of all registered servers. If any server is found to be unhealthy, the load balancer will stop sending requests to that server until it becomes healthy again.
8. **Making Requests to Load Balancer**:
   - Once health checks are performed and servers are marked as healthy, you can hit the load balancer, and it will relay the request to the backend servers. As shown in image below:
     ![](https://github.com/amarsharma441/load_balancer/tree/main/src/main/resources/static/no_server_registered.png)

9. **Changing Load Balancing Algorithm**:
   - The load balancer supports three algorithms:
        - Round-robin (default)
        - Random selection
        - Least connection
   - To change the algorithm, use the following API:
     ```bash
     PUT /switchAlgo
     Body: random / roundRobin / leastConnections
     ```
     As shown in image below:
     ![](https://github.com/amarsharma441/load_balancer/tree/main/src/main/resources/static/no_server_registered.png)
   - If you provide any other algorithm, the API will throw a bad request error.

**Note**: In the current implementation, the load balancer APIs for adding, removing servers, and switching algorithms do not yet support client authentication and authorization, presenting an opportunity for future enhancement.