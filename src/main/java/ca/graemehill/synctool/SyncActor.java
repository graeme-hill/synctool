package ca.graemehill.synctool;

import akka.actor.AbstractActor;
import akka.actor.Props;

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
}
