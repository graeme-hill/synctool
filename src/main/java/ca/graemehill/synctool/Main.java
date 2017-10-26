package ca.graemehill.synctool;

import akka.actor.ActorRef;
import ca.graemehill.synctool.actors.NetworkActor;
import ca.graemehill.synctool.actors.ScanActor;
import ca.graemehill.synctool.model.Collection;
import ca.graemehill.synctool.model.Node;
import ca.graemehill.synctool.model.NodeCollection;

import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        try {
            Sys.init("jdbc:sqlite:/home/graeme/temp/test-" + UUID.randomUUID() + ".sqlite");

            Collection photoCollection = new Collection(UUID.randomUUID(), "photos");
            Collections.putCollection(photoCollection);

            Node thisNode = Sys.getThisNode();
            NodeCollection dir1 = new NodeCollection(
                UUID.randomUUID(), thisNode.getId(), photoCollection.getId(), "/home/graeme/temp/one");
            NodeCollection dir2 = new NodeCollection(
                UUID.randomUUID(), thisNode.getId(), photoCollection.getId(), "/home/graeme/temp/two");
            Collections.putNodeCollection(dir1);
            Collections.putNodeCollection(dir2);

            Scan.fullScan();
        } catch (Exception e) {
            Log.fatal("boned", e);
            return;
        }

//        // Initialize dependencies
//        Sys.init("jdbc:sqlite::memory:");
//
//        // Migrate the database or bail if cannot
//        if (!Database.tryMigrate()) {
//            return;
//        }
//
//        // Setup self node
//        Node me = new Node(UUID.randomUUID(), "gdesktop", isMe);
//        ActorRef networkActor = Sys.getActorSystem().actorOf(NetworkActor.props());
//        networkActor.tell(new NetworkActor.NodeOnline(me), ActorRef.noSender());
//
//        // Trigger a scan
//        ActorRef scanActor = Sys.getActorSystem().actorOf(ScanActor.props());
//        scanActor.tell(new ScanRequest("/home/graeme/IdeaProjects/synctool"), ActorRef.noSender());

        // Don't end process until user hits enter
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        Sys.getActorSystem().terminate();
    }
}
