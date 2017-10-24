package ca.graemehill.synctool.model;

import java.util.UUID;

public class Transfer {
    private UUID nodeCollection;
    private UUID sourceNode;
    private UUID destinationNode;
    private int version;
    private long finished;

    public Transfer(UUID nodeCollection, UUID sourceNode, UUID destinationNode, int version, long finished) {
        this.nodeCollection = nodeCollection;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.version = version;
        this.finished = finished;
    }

    public UUID getNodeCollection() {
        return nodeCollection;
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
