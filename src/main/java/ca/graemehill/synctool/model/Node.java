package ca.graemehill.synctool.model;

import java.util.UUID;

public class Node {
    private UUID id;
    private String name;
    private boolean isMe;

    public Node(UUID id, String name, boolean isMe) {
        this.id = id;
        this.name = name;
        this.isMe = isMe;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isMe() {
        return isMe;
    }
}
