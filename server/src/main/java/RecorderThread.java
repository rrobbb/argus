import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class RecorderThread extends Thread {

    private static final String INPUT_FORMAT = "avfoundation"; // Apple API

    private static final String DEVICE_ID = "1";

    private static final int TARGET_FPS = 30;

    private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private final BlockingQueue<BufferedImage> outputQueue;

    public RecorderThread(BlockingQueue<BufferedImage> outputQueue) { this.outputQueue = outputQueue; }

    @Override
    public void run() {

        Frame frame;

        BufferedImage image;

        final long TARGET_DELAY_MS = 1000 / TARGET_FPS;

        try (final var grabber = createGrabber()) {

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

    private FFmpegFrameGrabber createGrabber() {

        var grabber = new FFmpegFrameGrabber(DEVICE_ID);

        grabber.setFormat(INPUT_FORMAT);
        grabber.setFrameRate(TARGET_FPS);
        grabber.setImageWidth(screenSize.width);
        grabber.setImageHeight(screenSize.height);

        return grabber;
    }
}
