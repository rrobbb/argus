import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class RecorderThread extends Thread {

    private static final String INPUT_FORMAT = getInputFormat();

    private static int FPS;

    private final BlockingQueue<BufferedImage> outputQueue;

    public RecorderThread(BlockingQueue<BufferedImage> outputQueue, final int FPS) {

        this.outputQueue = outputQueue;

        RecorderThread.FPS = FPS;
    }

    private static String getInputFormat() {

        final var OS = System.getProperty("os.name");

        return switch (OS) {

            case "Mac OS X" -> "avfoundation";

            case "Windows 10", "Windows 11" -> "gdigrab";

            default -> throw new IllegalStateException("Unexpected OS: " + OS);
        };
    }

    private FFmpegFrameGrabber createGrabber(String deviceID) {

        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        final var grabber = new FFmpegFrameGrabber(deviceID);

        grabber.setFormat(INPUT_FORMAT);
        grabber.setFrameRate(FPS);
        grabber.setImageWidth(screenSize.width);
        grabber.setImageHeight(screenSize.height);

        return grabber;
    }

    @Override
    public void run() {

        Frame frame;

        BufferedImage image;

        final long TARGET_DELAY_MS = 1000 / FPS;

        try (final var grabber = createGrabber("1")) {

            grabber.start();

            try (final var converter = new Java2DFrameConverter()) {

                while (!isInterrupted()) {

                    long startTime = System.currentTimeMillis();

                    frame = grabber.grab();

                    if (frame != null && frame.image != null) {

                        image = converter.getBufferedImage(frame);

                        outputQueue.poll();

                        boolean success = outputQueue.offer(image, 10, TimeUnit.MILLISECONDS);

                        if (!success) System.err.println("Frame not sent.");
                    }

                    long elapsedTime = System.currentTimeMillis() - startTime;

                    long sleepTime = TARGET_DELAY_MS - elapsedTime;

                    if (sleepTime > 0) Thread.sleep(sleepTime);
                }
            }

        } catch (IOException | InterruptedException ignored) {

            Thread.currentThread().interrupt();
        }
    }
}
