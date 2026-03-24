package dev.rob.argus.server.core;

import dev.rob.argus.server.Utils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.awt.*;
import java.io.OutputStream;

public final class ScreenRecorder implements AutoCloseable {

    private final FFmpegFrameGrabber grabber;

    private final FFmpegFrameRecorder recorder;

    private volatile boolean isRunning = false;

    /** For TCP communication */
    public ScreenRecorder(OutputStream out, int frameRate) {

        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        grabber = new FFmpegFrameGrabber("1");

        grabber.setFormat(Utils.getInputFormat());
        grabber.setFrameRate(frameRate);
        grabber.setImageWidth(screenSize.width);
        grabber.setImageHeight(screenSize.height);

        recorder = new FFmpegFrameRecorder(out, grabber.getImageWidth(), grabber.getImageHeight());

        // Mac OS options
        grabber.setOption("hwaccel", "videotoolbox");
        grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        recorder.setFormat("mpegts");
        recorder.setOption("color_range", "mpeg");
        recorder.setOption("tune", "zerolatency");
        recorder.setFrameRate(frameRate);
        recorder.setGopSize(frameRate * 2);
        recorder.setVideoBitrate(4000000);
    }

    /** For SRT/UDP communication */
    public ScreenRecorder(String url, int frameRate) {

        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        grabber = new FFmpegFrameGrabber("1");

        grabber.setFormat(Utils.getInputFormat());
        grabber.setFrameRate(frameRate);
        grabber.setImageWidth(screenSize.width);
        grabber.setImageHeight(screenSize.height);

        // for local testing
        recorder = new FFmpegFrameRecorder(url, grabber.getImageWidth(), grabber.getImageHeight());

        // Mac OS options
        grabber.setOption("hwaccel", "videotoolbox");
        grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        recorder.setFormat("mpegts");
        recorder.setOption("color_range", "mpeg");
        recorder.setOption("tune", "zerolatency");
        recorder.setFrameRate(frameRate);
        recorder.setGopSize(frameRate * 2);
        recorder.setVideoBitrate(4000000);
    }

    public double getFrameRate() { return grabber.getFrameRate(); }

    public void start() {

        if (isRunning) return;

        isRunning = true;

        if (grabber != null) try { grabber.start(); } catch (FFmpegFrameGrabber.Exception _) {}

        if (recorder != null) try { recorder.start(); } catch (FFmpegFrameRecorder.Exception _) {}
    }

    public void record() {

        if (!isRunning) return;

        if (grabber == null || recorder == null) return;

        try (var frame = grabber.grab()) {

            if (frame == null || frame.image == null) return;

            try { recorder.record(frame); } catch (FFmpegFrameRecorder.Exception _) {}

        } catch (FFmpegFrameGrabber.Exception _) {}
    }

    @Override
    public void close() {

        if (!isRunning) return;

        isRunning = false;

        if (grabber != null) try { grabber.stop(); } catch (FFmpegFrameGrabber.Exception _) {}

        finally { try { grabber.release(); } catch (FFmpegFrameGrabber.Exception _) {} }

        if (recorder != null) try { recorder.stop(); } catch (FFmpegFrameRecorder.Exception _) {}

        finally { try { recorder.release(); } catch (FFmpegFrameRecorder.Exception _) {} }
    }
}
