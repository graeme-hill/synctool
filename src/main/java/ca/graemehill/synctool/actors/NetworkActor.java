package ca.graemehill.synctool.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import ca.graemehill.synctool.model.Node;

public class NetworkActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return null;
    }

    public static Props props() {
        return null;
    }

    public static class NodeOnline {
        private Node node;

        public NodeOnline(Node node) {
            this.node = node;
        }

        public Node getNode() {
            return node;
        }
    }
}
