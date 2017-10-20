package ca.graemehill.synctool;

public class Log {
    public static void fatal(String msg) {
        System.out.println("FATAL: " + msg);
    }

    public static void fatal(String msg, Exception e) {
        System.out.println("FATAL: " + msg + "\n" + e.toString());
    }

    public static void error(String msg) {
        System.out.println("ERROR: " + msg);
    }

    public static void error(String msg, Exception e) {
        System.out.println("ERROR: " + msg + "\n" + e.toString());
    }

    public static void warning(String msg) {
        System.out.println("WARNING: " + msg);
    }

    public static void info(String msg) {
        System.out.println("INFO: " + msg);
    }

    public static void debug(String msg) {
        System.out.println("DEBUG: " + msg);
    }

    public static void trace(String msg) {
        System.out.println("TRACE: " + msg);
    }
}
