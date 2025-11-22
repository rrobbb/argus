package me.rob.rereborn.server;

public class Utils {

    public static String getInputFormat() {

        final var OS = System.getProperty("os.name");

        return switch (OS) {

            case "Mac OS X" -> "avfoundation";

            case "Windows 10", "Windows 11" -> "gdigrab";

            default -> throw new IllegalStateException("Unexpected OS: " + OS);
        };
    }



}
