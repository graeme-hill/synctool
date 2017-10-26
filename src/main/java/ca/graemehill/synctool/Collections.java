package ca.graemehill.synctool;

import ca.graemehill.synctool.model.Collection;
import ca.graemehill.synctool.model.NodeCollection;

public class Collections {
    public static void putCollection(Collection collection) throws Exception {
        try (Database db = new Database()) {
            db.put(collection);
        }
    }

    public static void putNodeCollection(NodeCollection nodeCollection) throws Exception {
        try (Database db = new Database()) {
            db.put(nodeCollection);
        }
    }
}
