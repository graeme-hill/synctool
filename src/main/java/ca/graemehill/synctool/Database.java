package ca.graemehill.synctool;

import ca.graemehill.synctool.model.Collection;
import ca.graemehill.synctool.model.FileMetadata;
import ca.graemehill.synctool.model.Node;
import ca.graemehill.synctool.model.NodeCollection;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class Database implements AutoCloseable {

    private Connection conn;

    public Database() throws SQLException {
        conn = DriverManager.getConnection(Sys.getConnectionString());
        conn.setAutoCommit(false);
    }

    public void put(NodeCollection nc) throws SQLException {
        runParameterizedCmd(
            "INSERT OR REPLACE INTO node_collections (id, node, collection, path) VALUES (?, ?, ?, ?)",
            statement -> {
                statement.setString(1, nc.getId().toString());
                statement.setString(2, nc.getNode().toString());
                statement.setString(3, nc.getCollection().toString());
                statement.setString(4, nc.getPath());
            }
        );
        conn.commit();
    }

    public void put(Collection c) throws SQLException {
        runParameterizedCmd(
            "INSERT OR REPLACE INTO collections (id, name) VALUES (?, ?)",
            statement -> {
                statement.setString(1, c.getId().toString());
                statement.setString(2, c.getName());
            }
        );
        conn.commit();
    }

    public void put(FileMetadata file) throws SQLException {
        runParameterizedCmd(
            "INSERT OR REPLACE INTO files (name, dir, size, created, modified) VALUES (?, ?, ?, ?, ?)",
            statement -> {
                statement.setString(1, file.getName());
                statement.setString(2, file.getDir());
                statement.setLong(3, file.getSize());
                statement.setLong(4, file.getCreated());
                statement.setLong(5, file.getModified());
            }
        );
        conn.commit();
    }

    public void put(Node node) throws SQLException {
        runParameterizedCmd(
            "INSERT OR REPLACE INTO nodes (id, name, is_me) VALUES (?, ?, ?)",
            statement -> {
                statement.setString(1, node.getId().toString());
                statement.setString(2, node.getName());
                statement.setInt(3, node.isMe() ? 1 : 0);
            }
        );
        conn.commit();
    }

    public Node getMyNode() throws SQLException {
        return runQuery(
            "SELECT id, name FROM nodes WHERE is_me=1",
            resultSet -> {
               if (resultSet.next()) {
                   return new Node(
                       UUID.fromString(resultSet.getString(1)),
                       resultSet.getString(2),
                       true
                   );
               } else {
                   return null;
               }
            });
    }

    public static void tryMigrate() throws Exception {
        Class.forName("org.sqlite.JDBC");

        try {
            try (Database db = new Database()) {
                db.migrate();
            } catch (Exception e) {
                Log.fatal("Could not run migrations.", e);
                throw e;
            }
        } catch (Exception e) {
            Log.fatal("Could not connect to database to run migrations.", e);
            throw e;
        }
    }

    public void migrate() throws SQLException {
        if (!tableExists("migrations")) {
            runCmd("CREATE TABLE migrations (version INT)");
            conn.commit();
        }

        String migration1 =
            "CREATE TABLE nodes (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "is_me INT NOT NULL);\n" +

            "CREATE TABLE file_metadatas (" +
                "version INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "name TEXT NOT NULL," +
                "dir TEXT NOT NULL," +
                "size INT NOT NULL," +
                "created INT NOT NULL," +
                "modified INT NOT NULL," +
                "checksum TEXT NULL);\n" +

            "CREATE TABLE collections (" +
                "id TEXT PRIMARY KEY NOT NULL," +
                "name TEXT NOT NULL);\n" +

            "CREATE TABLE node_collections (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "node TEXT NOT NULL," +
                "collection TEXT NOT NULL," +
                "path TEXT NOT NULL);\n" +

            "CREATE TABLE transfers (" +
                "node_collection TEXT," +
                "source_node TEXT," +
                "destination_node TEXT," +
                "version INT," +
                "finished TEXT);\n" +

            "CREATE TABLE config_log (" +
                "version INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "id TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "data TEXT NOT NULL);\n";

        runMigrations(new Migration[] {
            new Migration(1, migration1)
        });
    }

    private void runMigrations(Migration[] migrations) throws SQLException {
        for (Migration m : migrations) {
            if (!wasMigrationRunAlready(m.getVersion())) {
                migration(m.getVersion(), m.getSql());
            }
        }
    }

    private void migration(int version, String sql) throws SQLException {
        try {
            Log.info("Starting migration " + version);
            runCmd(sql);
            recordMigration(version);
            conn.commit();
            Log.info("Finished migration " + version);
        } catch (SQLException e) {
            Log.error("Migration " + version + " failed.", e);
            throw e;
        }
    }

    private void recordMigration(int version) throws SQLException {
        runParameterizedCmd("INSERT INTO migrations (version) VALUES (?)", statement -> {
            statement.setInt(1, version);
        });
    }

    private boolean wasMigrationRunAlready(int version) throws SQLException {
        return runParameterizedQuery(
            "SELECT 1 FROM migrations WHERE version=?",
            statement -> statement.setInt(1, version),
            resultSet -> resultSet.next());
    }

    public NodeCollection[] getNodeCollections(UUID nodeId) throws SQLException {
        return runParameterizedQuery(
            "SELECT id, collection, path FROM node_collections WHERE node=?",
            statement -> statement.setString(1, nodeId.toString()),
            resultSet -> {
                ArrayList<NodeCollection> result = new ArrayList<NodeCollection>();
                while (resultSet.next()) {
                    result.add(new NodeCollection(
                        UUID.fromString(resultSet.getString(1)),
                        nodeId,
                        UUID.fromString(resultSet.getString(2)),
                        resultSet.getString(3)
                    ));
                }
                return result.toArray(new NodeCollection[result.size()]);
            }
        );
    }

    private int runCmd(String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            return statement.executeUpdate(sql);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    private int runParameterizedCmd(String sql, SQLConsumer<PreparedStatement> withStatement) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(sql);
            withStatement.accept(statement);
            return statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    private <TResult> TResult runQuery(
        String sql,
        SQLFunction<ResultSet, TResult> withResultSet
    ) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(sql);
            return withResultSet.apply(resultSet);
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }

    private <TResult> TResult runParameterizedQuery(
        String sql,
        SQLConsumer<PreparedStatement> withStatement,
        SQLFunction<ResultSet, TResult> withResultSet
    ) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.prepareStatement(sql);
            withStatement.accept(statement);
            resultSet = statement.executeQuery();
            return withResultSet.apply(resultSet);
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        return runParameterizedQuery(
            "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",
            statement -> {
                statement.setString(1, tableName);
            },
            resultSet -> resultSet.next());
    }

    @Override
    public void close() throws Exception {
        conn.close();
    }

    public FileMetadata getFileMetadata(String name, String dir) throws SQLException {
        return runParameterizedQuery(
            "SELECT size, created, modified, checksum FROM file_metadatas WHERE name=? AND dir=?",
            statement -> {
                statement.setString(1, name);
                statement.setString(2, dir);
            },
            resultSet -> {
                if (resultSet.next()) {
                    long size = resultSet.getLong(1);
                    long created = resultSet.getLong(2);
                    long modified = resultSet.getLong(3);
                    String checksum = resultSet.getString(4);
                    return new FileMetadata(dir, name, size, created, modified, checksum);
                } else {
                    return null;
                }
            }
        );
    }

    public void replaceFileMetadata(FileMetadata newMetadata) throws SQLException {
        deleteFileMetadata(newMetadata.getName(), newMetadata.getDir());
        insertFileMetadata(newMetadata);
        conn.commit();
    }

    private void deleteFileMetadata(String name, String dir) throws SQLException {
        runParameterizedCmd(
            "DELETE FROM file_metadatas WHERE name=? AND dir=?",
            statement -> {
                statement.setString(1, name);
                statement.setString(2, dir);
            }
        );
    }

    private void insertFileMetadata(FileMetadata metadata) throws SQLException {
        runParameterizedCmd(
            "INSERT INTO file_metadatas (name, dir, size, created, modified, checksum) VALUES (?, ?, ?, ?, ?, ?)",
            statement -> {
                statement.setString(1, metadata.getName());
                statement.setString(2, metadata.getDir());
                statement.setLong(3, metadata.getSize());
                statement.setLong(4, metadata.getCreated());
                statement.setLong(5, metadata.getModified());
                statement.setString(6, metadata.getChecksum());
            }
        );
    }

    @FunctionalInterface
    public interface SQLConsumer<TValue> {
        void accept(TValue val) throws SQLException;
    }

    @FunctionalInterface
    public interface SQLFunction<TValue, TResult> {
        TResult apply(TValue val) throws SQLException;
    }

    public class Migration {
        private int version;
        private String sql;

        public Migration(int version, String sql) {
            this.version = version;
            this.sql = sql;
        }

        public int getVersion() {
            return version;
        }

        public String getSql() {
            return sql;
        }
    }
}
