package ca.graemehill.synctool;

import akka.actor.ActorSystem;
import ca.graemehill.synctool.model.Node;

public class Sys {
    private static ActorSystem actorSystem;
    private static String connectionString;
    private static Node thisNode;

    public static void init(String connStr) throws Exception {
        connectionString = connStr;
        actorSystem = ActorSystem.create();
        Database.tryMigrate();
        thisNode = Nodes.requireSelf();
    }

    public static ActorSystem getActorSystem() {
        return actorSystem;
    }

    public static String getConnectionString() {
        return connectionString;
    }

    public static Node getThisNode() {
        return thisNode;
    }
}
