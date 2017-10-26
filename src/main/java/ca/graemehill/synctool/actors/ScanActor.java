package ca.graemehill.synctool.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import ca.graemehill.synctool.Log;
import ca.graemehill.synctool.Sys;
import ca.graemehill.synctool.model.FileMetadata;
import ca.graemehill.synctool.model.NodeCollection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class ScanActor extends AbstractActor {
    private ActorRef fileMetadataActor;

    public static Props props() {
        return Props.create(ScanActor.class);
    }

    public ScanActor() {
        fileMetadataActor = Sys.getActorSystem().actorOf(FileMetadataActor.props());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ScanRequest.class, scanRequest -> {
                scan(scanRequest);
            })
            .matchAny(msg -> {
                Log.error("Unknown message " + msg.toString());
            })
            .build();
    }

    private void scan(ScanRequest req) {
        try {
            scan(req.getNodeCollection().getPath(), -1);
        } catch (FileNotFoundException e) {
            Log.error("Could not scan " + req.getNodeCollection().getPath() + " because it doesn't exist.");
        }
    }

    private void scan(String pathStr, int depth) throws FileNotFoundException {
        File file = new File(pathStr);

        if (!file.exists()) {
            throw new FileNotFoundException(pathStr);
        }

        if (file.isDirectory()) {
            scanDirectory(file, depth);
        } else {
            consumeFile(file.toPath());
        }

    }

    private void scanDirectory(File file, int depth) {
        Path path = file.toPath();
        try {
            if (depth < 1) {
                Files.walk(file.toPath()).forEach(this::consumeFile);
            } else {
                Files.walk(file.toPath(), depth).forEach(this::consumeFile);
            }
        } catch (IOException e) {
            Log.error("scanRecursive failed for path " + path.toString(), e);
        }
    }

    private void consumeFile(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            FileMetadata metadata = new FileMetadata(
                path.getParent().toString(),
                path.getFileName().toString(),
                attrs.size(),
                attrs.creationTime().toMillis(),
                attrs.lastModifiedTime().toMillis(),
                null);
            fileMetadataActor.tell(new FileMetadataActor.FileDiscovered(metadata), getSelf());
        } catch (IOException e) {
            Log.error("consumeFile failed for path " + path.toString(), e);
        }
    }

    public static class ScanRequest {
        private NodeCollection nodeCollection;

        public ScanRequest(NodeCollection nodeCollection) {
            this.nodeCollection = nodeCollection;
        }

        public NodeCollection getNodeCollection() {
            return nodeCollection;
        }
    }

//    public static class ScanRequest_ {
//        private String directory;
//        private int depth;
//
//        public ScanRequest(String directory) {
//            this(directory, -1);
//        }
//
//        public ScanRequest(String directory, int depth) {
//            this.directory = directory;
//            this.depth = depth;
//        }
//
//        public String getDirectory() {
//            return directory;
//        }
//
//        public int getDepth() {
//            return depth;
//        }
//    }
}
