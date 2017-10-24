package ca.graemehill.synctool;

import akka.actor.ActorRef;
import ca.graemehill.synctool.model.Node;
import org.junit.Assert;
import org.junit.Test;

import java.util.Scanner;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestScanner {

    @Test
    public void testScan() {
        // Initialize dependencies
        Sys.init("jdbc:sqlite::memory:");
        Assert.assertTrue(Metadatabase.tryMigrate());

        

        // Setup self node
        Node me = new Node(UUID.randomUUID(), "gdesktop");
        ActorRef networkActor = Sys.getActorSystem().actorOf(NetworkActor.props());
        networkActor.tell(new NodeOnline(me), ActorRef.noSender());

        // Trigger a scan
        ActorRef scanActor = Sys.getActorSystem().actorOf(ScanActor.props());
        scanActor.tell(new ScanRequest("/home/graeme/IdeaProjects/synctool"), ActorRef.noSender());

        // Add a policy


        // Don't end process until user hits enter
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        Sys.getActorSystem().terminate();

        /////////////////////////////////

        assertEquals(1, 1);
    }
}
