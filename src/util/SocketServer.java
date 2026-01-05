package util;

/**
 * Notifications removed: provide a harmless no-op stub to preserve references.
 */
public class SocketServer {
    public static void start(int port) { /* no-op */ }
    public static void stop() { /* no-op */ }
    public static void broadcast(String msg) { /* no-op */ }
    public static boolean sendToEmployee(int employeeId, String msg) { return false; }
}