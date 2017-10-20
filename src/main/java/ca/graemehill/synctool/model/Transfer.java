package ca.graemehill.synctool.model;

import java.util.UUID;

public class Transfer {
    private UUID policy;
    private UUID sourceNode;
    private UUID destinationNode;
    private int version;
    private long finished;

    public Transfer(UUID policy, UUID sourceNode, UUID destinationNode, int version, long finished) {
        this.policy = policy;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.version = version;
        this.finished = finished;
    }

    public UUID getPolicy() {
        return policy;
    }

    public UUID getSourceNode() {
        return sourceNode;
    }

    public UUID getDestinationNode() {
        return destinationNode;
    }

    public int getVersion() {
        return version;
    }

    public long getFinished() {
        return finished;
    }
}
