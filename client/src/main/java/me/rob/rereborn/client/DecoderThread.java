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

        try (final var grabber = new FFmpegFrameGrabber(in, 0)) {

            grabber.setFormat("mpegts");
            grabber.setVideoCodecName("h264_videotoolbox");
            grabber.setOption("analyzeduration", "5000000");
            grabber.setOption("probesize", "1000000");
            grabber.start();

            try (final var converter = new Java2DFrameConverter()) {

                Frame frame;

                BufferedImage decodedImage;

                while (!isInterrupted() && (frame = grabber.grab()) != null) {

                    if (frame.image != null) {
                        decodedImage = converter.getBufferedImage(frame);
                        out.offer(decodedImage);
                    }
                }
            }

            grabber.stop();

        } catch (Exception e) {

            System.err.println(e.getMessage());

            Thread.currentThread().interrupt();
        }
    }
}