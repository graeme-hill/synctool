package ca.graemehill.synctool;

import akka.actor.ActorSystem;

public class Sys {
    private static ActorSystem actorSystem;
    private static String connectionString;

    public static void init(String connStr) {
        connectionString = connStr;
        actorSystem = ActorSystem.create();
    }

    public static ActorSystem getActorSystem() {
        return actorSystem;
    }

    public static String getConnectionString() {
        return connectionString;
    }
}
