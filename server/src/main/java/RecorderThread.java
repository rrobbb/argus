import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

public class RecorderThread extends Thread {

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

        try {

            while (true) {

                var image = robot.createScreenCapture(screenRect);

                imageQueue.put(image);

            }

        } catch (InterruptedException ignored) {}
    }
}
