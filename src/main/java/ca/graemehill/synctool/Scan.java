package ca.graemehill.synctool;

import akka.actor.ActorRef;
import ca.graemehill.synctool.actors.ScanActor;
import ca.graemehill.synctool.model.Node;
import ca.graemehill.synctool.model.NodeCollection;

public class Scan {
    public static void fullScan() throws Exception {
        try (Database db = new Database()) {
            Node me = db.getMyNode();
            NodeCollection[] nodeCollections = db.getNodeCollections(me.getId());

            if (nodeCollections.length > 0) {
                ActorRef scanActor = Sys.getActorSystem().actorOf(ScanActor.props());
                for (NodeCollection nodeCollection : nodeCollections) {
                    scanActor.tell(new ScanActor.ScanRequest(nodeCollection), ActorRef.noSender());
                }
            }
        }
    }
}
