package ca.graemehill.synctool;

public class ScanRequest {
    private String directory;
    private int depth;

    public ScanRequest(String directory) {
        this(directory, -1);
    }

    public ScanRequest(String directory, int depth) {
        this.directory = directory;
        this.depth = depth;
    }

    public String getDirectory() {
        return directory;
    }

    public int getDepth() {
        return depth;
    }
}
