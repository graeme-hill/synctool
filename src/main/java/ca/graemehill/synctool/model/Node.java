package ca.graemehill.synctool.model;

import java.util.UUID;

public class Node {
    private UUID id;
    private String name;

    public Node(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
