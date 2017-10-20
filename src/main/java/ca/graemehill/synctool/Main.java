package ca.graemehill.synctool;

import akka.actor.ActorRef;
import ca.graemehill.synctool.model.Node;

import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        // Initialize dependencies
        Sys.init("jdbc:sqlite::memory:");

        // Migrate the database or bail if cannot
        if (!Metadatabase.tryMigrate()) {
            return;
        }

        // Setup self node
        Node me = new Node(UUID.randomUUID(), "gdesktop");


        // Trigger a scan
        ActorRef scanActor = Sys.getActorSystem().actorOf(ScanActor.props());
        scanActor.tell(new ScanRequest("/home/graeme/IdeaProjects/synctool"), ActorRef.noSender());

        // Add a policy


        // Don't end process until user hits enter
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        Sys.getActorSystem().terminate();
    }
}
