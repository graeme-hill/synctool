package ca.graemehill.synctool;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {
    private byte[] bytes;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public Checksum(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getString() {
        return bytesToHex(getBytes());
    }

    public static Checksum calc(String path) throws IOException, NoSuchAlgorithmException {
        File file = new File(path);

        if (file.isDirectory()) {
            return null;
        }

        try (InputStream in = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return new Checksum(digest.digest());
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
