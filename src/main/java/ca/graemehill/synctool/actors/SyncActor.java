package ca.graemehill.synctool.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import ca.graemehill.synctool.Log;
import ca.graemehill.synctool.model.FileMetadata;

public class SyncActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(FileReadyToSync.class, msg -> {
                Log.info("File ready to sync: " + msg.getFileMetadata().getName());
            })
            .matchAny(msg -> {
                Log.warning("SyncActor received unknown message");
            })
            .build();
    }

    public static Props props() {
        return Props.create(SyncActor.class);
    }

    public static class FileReadyToSync {
        private FileMetadata fileMetadata;

        public FileReadyToSync(FileMetadata fileMetadata) {
            this.fileMetadata = fileMetadata;
        }

        public FileMetadata getFileMetadata() {
            return fileMetadata;
        }
    }
}
