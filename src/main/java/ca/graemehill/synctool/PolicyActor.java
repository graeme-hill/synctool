package ca.graemehill.synctool;

import akka.actor.AbstractActor;
import ca.graemehill.synctool.model.Policy;

public class PolicyActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(CreatePolicy.class, this::createPolicy)
            .build();
    }

    private void createPolicy(CreatePolicy msg) {
        try {
            try (Metadatabase db = new Metadatabase()) {
                db.put(msg.getPolicy());
            }
        } catch (Exception e) {
            Log.error("Failed to create policy.", e);
        }
    }

    public class CreatePolicy {
        private Policy policy;

        public CreatePolicy(Policy policy) {
            this.policy = policy;
        }

        public Policy getPolicy() {
            return policy;
        }
    }
}
