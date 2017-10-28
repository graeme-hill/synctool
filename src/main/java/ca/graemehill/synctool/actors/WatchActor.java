package ca.graemehill.synctool.actors;

import akka.actor.AbstractActor;
import ca.graemehill.synctool.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

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
        try {
            watcher.watch(msg.getPath());
        } catch (Exception e) {
            Log.error("Would not watch " + msg.getPath(), e);
        }
    }

    private void stopWatching(StopWatching msg) {
        watcher.stop(msg.getPath());
    }

    public static class Watcher {
        public void watch(String path) throws IOException {
            Log.info("WATCH " + path);
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path pathToWatch = new File(path).toPath();
            WatchKey key = pathToWatch.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
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
