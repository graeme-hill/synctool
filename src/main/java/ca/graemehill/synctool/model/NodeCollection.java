package ca.graemehill.synctool.model;

import java.util.UUID;

public class NodeCollection {
    private UUID id;
    private UUID node;
    private UUID collection;
    private String path;

    public NodeCollection(UUID id, UUID node, UUID collection, String path) {
        this.id = id;
        this.node = node;
        this.collection = collection;
        this.path = path;
    }

    public UUID getCollection() {
        return collection;
    }

    public UUID getNode() {
        return node;
    }

    public String getPath() {
        return path;
    }

    public UUID getId() {
        return id;
    }
}
