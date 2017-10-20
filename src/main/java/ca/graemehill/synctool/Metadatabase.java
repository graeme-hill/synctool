package ca.graemehill.synctool;

import ca.graemehill.synctool.model.FileMetadata;
import ca.graemehill.synctool.model.Policy;

import java.sql.*;

public class Metadatabase implements AutoCloseable {

    private Connection conn;

    public Metadatabase() throws SQLException {
        conn = DriverManager.getConnection(Sys.getConnectionString());
        //conn.setAutoCommit(true);
    }

    public void put(Policy policy) throws SQLException {
        runParameterizedCmd(
            "INSERT OR REPLACE INTO policies (id, sourceNode, destinationNode, sourcePath, destinationPath) " +
                "VALUES (?, ?, ?, ?, ?)",
            statement -> {
                statement.setString(1, policy.getId().toString());
                statement.setString(2, policy.getSourceNode().toString());
                statement.setString(3, policy.getDestinationNode().toString());
                statement.setString(4, policy.getSourcePath());
                statement.setString(5, policy.getDestinationPath());
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
    }

    public static boolean tryMigrate() {
        try {
            try (Metadatabase db = new Metadatabase()) {
                db.migrate();
                return true;
            } catch (Exception e) {
                Log.fatal("Could not run migrations.", e);
                return false;
            }
        } catch (Exception e) {
            Log.fatal("Could not connect to database to run migrations.", e);
            return false;
        }
    }

    public void migrate() throws SQLException {
        if (!tableExists("migrations")) {
            runCmd("CREATE TABLE migrations (version INT)");
            conn.commit();
        }

        String migration1 =
            "CREATE TABLE nodes (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT);" +
            "CREATE TABLE file_metadatas (" +
                "version INT PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "name TEXT NOT NULL," +
                "dir TEXT NOT NULL," +
                "created INT NOT NULL," +
                "modified INT NOT NULL," +
                "checksum TEXT NOT NULL);" +
            "CREATE TABLE collections (" +
                "id TEXT PRIMARY KEY NOT NULL," +
                "name TEXT NOT NULL);" +
            "CREATE TABLE node_collections (" +
                "node TEXT NOT NULL," +
                "collection TEXT NOT NULL," +
                "path TEXT NOT NULL);" +
            "CREATE TABLE policies (" +
                "id TEXT PRIMARY KEY," +
                "sourceNode TEXT," +
                "destinationNode TEXT," +
                "sourcePath TEXT," +
                "destinationPath TEXT);" +
            "CREATE TABLE transfers (" +
                "policy TEXT," +
                "sourceNode TEXT," +
                "destinationNode TEXT," +
                "version INT," +
                "finished TEXT);";

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
                    long size = resultSet.getLong(0);
                    long created = resultSet.getLong(1);
                    long modified = resultSet.getLong(2);
                    String checksum = resultSet.getString(3);
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
