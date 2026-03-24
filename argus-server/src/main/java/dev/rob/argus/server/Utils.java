package dev.rob.argus.server;

public class Utils {

    public static String getInputFormat() {

        final var OS = System.getProperty("os.name");

        return switch (OS) {

            case "Mac OS X" -> "AVfoundation";

            case "Windows 10", "Windows 11" -> "gdigrab";

            default -> throw new IllegalStateException("Unexpected OS: " + OS);
        };
    }
}
