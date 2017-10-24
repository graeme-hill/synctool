package ca.graemehill.synctool;

import akka.actor.AbstractActor;
import ca.graemehill.synctool.model.NodeCollection;

public class PolicyActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(PutNodeCollection.class, this::putNodeCollection)
            .build();
    }

    private void putNodeCollection(PutNodeCollection msg) {
        try {
            try (Metadatabase db = new Metadatabase()) {
                db.put(msg.getNodeCollection());
            }
        } catch (Exception e) {
            Log.error("Failed to create node collection.", e);
        }
    }

    public class PutNodeCollection {
        private NodeCollection nodeCollection;

        public PutNodeCollection(NodeCollection nodeCollection) {
            this.nodeCollection = nodeCollection;
        }

        public NodeCollection getNodeCollection() {
            return nodeCollection;
        }
    }
}
