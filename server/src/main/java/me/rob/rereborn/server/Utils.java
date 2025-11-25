package me.rob.rereborn.server;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class Utils {

    private static final int BLOCK_SIZE = 16, PIXEL_DIFF_THRESHOLD = 20;

    private static final int NEUTRAL_COLOR = 0xFF000000;

    public static String getInputFormat() {

        final var OS = System.getProperty("os.name");

        return switch (OS) {

            case "Mac OS X" -> "avfoundation";

            case "Windows 10", "Windows 11" -> "gdigrab";

            default -> throw new IllegalStateException("Unexpected OS: " + OS);
        };
    }

    public static boolean calculateDelta(BufferedImage currentImage, BufferedImage lastImage) {

        final int[] currentData = ((DataBufferInt) currentImage.getRaster().getDataBuffer()).getData();
        final int[] lastData = ((DataBufferInt) lastImage.getRaster().getDataBuffer()).getData();

        final int width = currentImage.getWidth();
        final int height = currentImage.getHeight();

        boolean hasChanged = false;

        for (int y = 0; y < height; y += BLOCK_SIZE) {

            for (int x = 0; x < width; x += BLOCK_SIZE) {

                boolean blockChanged = false;

                final int endY = Math.min(y + BLOCK_SIZE, height);
                final int endX = Math.min(x + BLOCK_SIZE, width);

                for (int i = y; i < endY; i++) {

                    for (int j = x; j < endX; j++) {

                        int index = i * width + j;

                        int diff = getDiff(currentData, lastData, index);

                        if (diff > PIXEL_DIFF_THRESHOLD) {
                            blockChanged = true;
                            hasChanged = true;
                            break;
                        }
                    }

                    if (blockChanged) break;
                }

                if (!blockChanged) {

                    for (int i = y; i < endY; i++) {

                        int rowStartIndex = i * width + x;

                        Arrays.fill(currentData, rowStartIndex, rowStartIndex + (endX - x), NEUTRAL_COLOR);
                    }
                }
            }
        }

        return hasChanged;
    }

    private static int getDiff(int[] currentData, int[] previousData, int index) {

        int curr = currentData[index];
        int prev = previousData[index];

        int r_curr = (curr >> 16) & 0xFF;
        int g_curr = (curr >> 8) & 0xFF;
        int b_curr = curr & 0xFF;

        int r_prev = (prev >> 16) & 0xFF;
        int g_prev = (prev >> 8) & 0xFF;
        int b_prev = prev & 0xFF;

        return Math.abs(r_curr - r_prev) + Math.abs(g_curr - g_prev) + Math.abs(b_curr - b_prev);
    }


}
