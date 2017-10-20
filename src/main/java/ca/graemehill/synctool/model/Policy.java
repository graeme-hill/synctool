package ca.graemehill.synctool.model;

import java.util.UUID;

public class Policy {
    private UUID id;
    private UUID sourceNode;
    private UUID destinationNode;
    private String sourcePath;
    private String destinationPath;

    public Policy(UUID id, UUID sourceNode, UUID destinationNode, String sourcePath, String destinationPath) {
        this.id = id;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceNode() {
        return sourceNode;
    }

    public UUID getDestinationNode() {
        return destinationNode;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }
}
