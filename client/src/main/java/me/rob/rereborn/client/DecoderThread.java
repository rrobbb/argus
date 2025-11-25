package me.rob.rereborn.client;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

public final class DecoderThread extends Thread {

    private final InputStream in;

    private final BlockingQueue<BufferedImage> out;

    public DecoderThread(InputStream in, BlockingQueue<BufferedImage> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        long frameCount = 0;

        long lastTime = System.currentTimeMillis();

        try (final var grabber = getGrabber()) {

            grabber.start();

            try (final var converter = new Java2DFrameConverter()) {

                Frame frame;

                BufferedImage decodedImage;

                while (!isInterrupted() && (frame = grabber.grab()) != null) {

                    if (frame.image != null) {

                        long currentTime = System.currentTimeMillis();

                        long elapsedTime = currentTime - lastTime;

                        decodedImage = converter.getBufferedImage(frame);

                        frameCount++;

                        out.offer(decodedImage);

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

    private FFmpegFrameGrabber getGrabber() {

        final var grabber = new FFmpegFrameGrabber(in, 0);

        grabber.setFormat("mpegts");
        grabber.setVideoCodecName("h264_videotoolbox");
        grabber.setOption("analyzeduration", "5000000");
        grabber.setOption("probesize", "1000000");

        return grabber;
    }
}