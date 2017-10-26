package ca.graemehill.synctool.model;

import java.util.UUID;

public class Collection {
    private UUID id;
    private String name;

    public Collection(UUID id, String name) {
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
