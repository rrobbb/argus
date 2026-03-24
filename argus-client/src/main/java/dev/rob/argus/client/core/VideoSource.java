package dev.rob.argus.client.core;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.InputStream;

public final class VideoSource implements AutoCloseable {

    private final FFmpegFrameGrabber grabber;

    private boolean isRunning = false;

    /** For TCP communication */
    public VideoSource(InputStream in) {

        grabber = new FFmpegFrameGrabber(in, 0);

        configure();
    }

    /** For SRT/UDP communication */
    public VideoSource(String url) {

        grabber = new FFmpegFrameGrabber(url);

        configure();
    }

    private void configure() {
        grabber.setFormat("mpegts");
        grabber.setVideoCodecName("h264_videotoolbox");
        grabber.setOption("analyzeduration", "5000000");
        grabber.setOption("probesize", "1000000");
    }

    public void start() {

        if (isRunning) return;

        isRunning = true;

        if (grabber != null) try { grabber.start(); } catch (FFmpegFrameGrabber.Exception _) {}
    }

    public Frame grab() {

        if (!isRunning) return null;

        Frame frame = null;

        try { frame = grabber.grab(); } catch (FFmpegFrameGrabber.Exception _) {}

        return frame;
    }

    @Override
    public void close() {

        if (!isRunning) return;

        isRunning = false;

        if (grabber != null) try { grabber.stop(); } catch (FFmpegFrameGrabber.Exception _) {}

        finally { try { grabber.release(); } catch (FFmpegFrameGrabber.Exception _) {} }
    }
}
