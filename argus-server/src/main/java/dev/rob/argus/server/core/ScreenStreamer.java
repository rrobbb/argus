package dev.rob.argus.server.core;

public final class ScreenStreamer implements Runnable {

    private final ScreenRecorder recorder;

    public ScreenStreamer(ScreenRecorder recorder) { this.recorder = recorder; }

    @Override
    public void run() {

        final long NS_PER_FRAME = 1_000_000_000L / (int) recorder.getFrameRate();

        try (recorder) {

            recorder.start();

            long nextFrameTime = System.nanoTime();

            while (!Thread.currentThread().isInterrupted()) {

                recorder.record();

                nextFrameTime += NS_PER_FRAME;

                long sleepTimeNs = nextFrameTime - System.nanoTime();

                if (sleepTimeNs > 0) {
                    Thread.sleep(sleepTimeNs / 1_000_000, (int) (sleepTimeNs % 1_000_000));
                } else {
                    nextFrameTime = System.nanoTime();
                }
            }

        } catch (InterruptedException ignored) {}
    }
}
