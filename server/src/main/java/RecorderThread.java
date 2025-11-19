import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

public final class RecorderThread extends Thread {

    private final Robot robot;

    private final Rectangle screenRect;

    private final BlockingQueue<BufferedImage> imageQueue;

    public RecorderThread(BlockingQueue<BufferedImage> imageQueue) throws AWTException {

        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        robot = new Robot();

        screenRect = new Rectangle(screenSize);

        this.imageQueue = imageQueue;
    }

    @Override
    public void run() {

        final int TARGET_FPS = 60;

        final long TARGET_DELAY_MS = 1000 / TARGET_FPS;

        BufferedImage image;

        try {

            while (!isInterrupted()) {

                long startTime = System.currentTimeMillis();

                image = robot.createScreenCapture(screenRect);

                imageQueue.offer(image);

                long endTime = System.currentTimeMillis();

                long elapsedTime = endTime - startTime;

                long sleepTime = TARGET_DELAY_MS - elapsedTime;

                if (sleepTime > 0) Thread.sleep(sleepTime);
            }

        } catch (InterruptedException ignored) {

            Thread.currentThread().interrupt();
        }
    }
}
