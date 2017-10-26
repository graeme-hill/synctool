package ca.graemehill.synctool.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import ca.graemehill.synctool.model.ConfigLogEntry;

public class ConfigActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return null;
    }

    public static Props props() {
        return null;
    }

    public static class NewConfigLogEntry {
        private ConfigLogEntry logEntry;

        public NewConfigLogEntry(ConfigLogEntry logEntry) {
            this.logEntry = logEntry;
        }

        public ConfigLogEntry getLogEntry() {
            return logEntry;
        }
    }
}
