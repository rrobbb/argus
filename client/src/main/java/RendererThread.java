import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedBlockingQueue;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public final class RendererThread extends Thread {

    private final JFrame frame = new JFrame("Client");

    private final LinkedBlockingQueue<BufferedImage> imageQueue;

    private final BufferStrategy strategy;

    public RendererThread(LinkedBlockingQueue<BufferedImage> imageQueue) {

        this.imageQueue = imageQueue;

        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setLocationRelativeTo(null);

        var canvas = new Canvas();

        canvas.setIgnoreRepaint(true);
        canvas.setSize(frame.getWidth(), frame.getHeight());

        frame.add(canvas);

        frame.setVisible(true);

        canvas.createBufferStrategy(3);

        strategy = canvas.getBufferStrategy();
    }

    @Override
    public void run() {

        while (!isInterrupted()) {

            try {

                var image = imageQueue.take();

                if (strategy == null) continue;

                var g = strategy.getDrawGraphics();

                g.drawImage(image, 0, 0, frame.getWidth(), frame.getHeight(), null);
                g.dispose();

                strategy.show();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
