package ca.graemehill.synctool.actors;

import akka.actor.AbstractActor;
import ca.graemehill.synctool.Log;

public class WatchActor extends AbstractActor {
    private static Watcher watcher = new Watcher();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(StartWatching.class, this::watch)
            .match(StopWatching.class, this::stopWatching)
            .build();
    }

    private void watch(StartWatching msg) {
        watcher.watch(msg.getPath());
    }

    private void stopWatching(StopWatching msg) {
        watcher.stop(msg.getPath());
    }

    public static class Watcher {
        public void watch(String path) {
            Log.info("WATCH " + path);
        }
        public void stop(String path) {
            Log.info("STOP WATCHING " + path);
        }
    }

    public class StartWatching {
        private String path;

        public StartWatching(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    public class StopWatching {
        private String path;

        public StopWatching(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }
}
