package dev.rob.argus.client.core;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

public final class VideoDecoder implements Runnable {

    private final VideoSource source;

    private final BlockingQueue<BufferedImage> imageQueue;

    public VideoDecoder(VideoSource source, BlockingQueue<BufferedImage> imageQueue) {
        this.source = source;
        this.imageQueue = imageQueue;
    }

    @Override
    public void run() {

        long frameCount = 0;

        long lastTime = System.currentTimeMillis();

        try (source) {

            source.start();

            try (final var converter = new Java2DFrameConverter()) {

                Frame frame;

                BufferedImage image;

                while (!Thread.currentThread().isInterrupted() && (frame = source.grab()) != null) {

                    if (frame.image != null) {

                        long currentTime = System.currentTimeMillis();

                        long elapsedTime = currentTime - lastTime;

                        image = converter.getBufferedImage(frame);

                        frameCount++;

                        if (!imageQueue.offer(image)) {
                            imageQueue.poll();
                            imageQueue.offer(image);
                        }

                        if (elapsedTime >= 1000) {

                            double fps = (double) frameCount * 1000.0 / elapsedTime;

                            System.out.printf("FPS: %.2f\n", fps);

                            frameCount = 0;
                            lastTime = currentTime;
                        }
                    }
                }
            }

        } catch (Exception e) {

            System.err.println(e.getMessage());

            Thread.currentThread().interrupt();
        }
    }
}