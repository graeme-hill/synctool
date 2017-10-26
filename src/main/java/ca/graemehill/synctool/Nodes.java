package ca.graemehill.synctool;

import akka.actor.ActorRef;
import ca.graemehill.synctool.actors.NetworkActor;
import ca.graemehill.synctool.model.Node;

import java.sql.SQLException;
import java.util.UUID;

public class Nodes {
    public static Node requireSelf() throws Exception {
        try {
            try (Database db = new Database()) {
                Node me = db.getMyNode();
                if (me == null) {
                    Node newMe = new Node(UUID.randomUUID(), "name-goes-here", true);
                    db.put(newMe);
                    return newMe;
                } else {
                    return me;
                }
            }
        } catch (Exception e) {
            Log.fatal("Could not find/create self node", e);
            throw e;
        }
    }
}
