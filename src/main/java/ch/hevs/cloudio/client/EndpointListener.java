package ch.hevs.cloudio.client;

public interface EndpointListener {
    void endpointConnectionStatusChanged(Endpoint endpoint, boolean online);
}
