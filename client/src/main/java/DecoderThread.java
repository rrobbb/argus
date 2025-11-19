import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class DecoderThread extends Thread {

    private final BlockingQueue<byte[]> decodingQueue;

    private final BlockingQueue<BufferedImage> imagesQueue;

    public DecoderThread(BlockingQueue<byte[]> decodingQueue, BlockingQueue<BufferedImage> imagesQueue) {
        this.decodingQueue = decodingQueue;
        this.imagesQueue = imagesQueue;
    }

    @Override
    public void run() {

        byte[] imageData;

        BufferedImage image;

        FFmpegFrameGrabber grabber;

        Frame frame;

        try (final var converter = new Java2DFrameConverter()) {

            while (!isInterrupted()) {

                imageData = decodingQueue.take();

                try (final var inputStream = new ByteArrayInputStream(imageData)) {

                    grabber = new FFmpegFrameGrabber(inputStream);

                    grabber.setFormat("mjpeg");
                    grabber.setOption("vframes", "1");

                    grabber.start();

                    frame = grabber.grabImage();

                    if (frame != null) {

                        image = converter.getBufferedImage(frame);

                        boolean success = imagesQueue.offer(image, 100, TimeUnit.MILLISECONDS);

                        if (!success) {
                            System.err.println("Display Queue Full. Frame discarded by Decoder.");
                        }
                    }
                }
            }

        } catch (InterruptedException | IOException ignored) {

            Thread.currentThread().interrupt();
        }
    }
}
