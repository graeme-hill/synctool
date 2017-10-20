package ca.graemehill.synctool.model;

public class FileMetadata {
    private String dir;
    private String name;
    private long size;
    private long created;
    private long modified;
    private String checksum;

    public FileMetadata(String dir, String name, long size, long created, long modified, String checksum) {
        this.dir = dir;
        this.name = name;
        this.size = size;
        this.created = created;
        this.modified = modified;
        this.checksum = checksum;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getDir() {
        return dir;
    }

    public long getCreated() {
        return created;
    }

    public long getModified() {
        return modified;
    }

    public String getChecksum() {
        return checksum;
    }
}
