package ca.graemehill.synctool.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import ca.graemehill.synctool.Checksum;
import ca.graemehill.synctool.Log;
import ca.graemehill.synctool.Database;
import ca.graemehill.synctool.Sys;
import ca.graemehill.synctool.model.FileMetadata;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class FileMetadataActor extends AbstractActor {
    private ActorRef syncActor;

    public FileMetadataActor() {
        syncActor = Sys.getActorSystem().actorOf(SyncActor.props());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(FileDiscovered.class, fd -> {
                Log.info("discovered " + fd.getMetadata().getName() + " at " + fd.getMetadata().getDir());
                onFileDiscovered(fd.getMetadata());
            })
            .matchAny(msg -> {
                Log.warning("received unknown message: " + msg.toString());
            })
            .build();
    }

    private void onFileDiscovered(FileMetadata newMetadata) {
        try {
            try (Database db = new Database()) {
                FileMetadata existingMetadata = db.getFileMetadata(newMetadata.getName(), newMetadata.getDir());
                if (!closeEnough(existingMetadata, newMetadata)) {
                    FileMetadata newMetadataWithChecksum = getMetadataWithChecksum(newMetadata);
                    db.replaceFileMetadata(newMetadataWithChecksum);
                    syncActor.tell(new SyncActor.FileReadyToSync(newMetadata), getSelf());
                }
            }
        } catch (Exception e) {
            Log.error("onFileDiscovered failed", e);
        }
    }

    private FileMetadata getMetadataWithChecksum(FileMetadata without) throws NoSuchAlgorithmException, IOException {
        String filePath = Paths.get(without.getDir(), without.getName()).toString();
        Checksum checksum = Checksum.calc(filePath);
        return new FileMetadata(
            without.getDir(),
            without.getName(),
            without.getSize(),
            without.getCreated(),
            without.getModified(),
            checksum == null ? null : checksum.getString());
    }

    private boolean closeEnough(FileMetadata a, FileMetadata b) {
        // one or both are null
        if (a == null ^ b == null) {
            return false;
        } else if (a == null && b == null) {
            return true;
        }

        // neither are null
        if (a.getChecksum() != null && b.getChecksum() != null) {
            return a.getChecksum() == b.getChecksum();
        } else {
            return a.getSize() == b.getSize() && a.getModified() == b.getModified();
        }
    }

    public static Props props() {
        return Props.create(FileMetadataActor.class);
    }

    public static class FileDiscovered {
        private FileMetadata metadata;

        public FileDiscovered(FileMetadata metadata) {
            this.metadata = metadata;
        }

        public FileMetadata getMetadata() {
            return metadata;
        }
    }
}
