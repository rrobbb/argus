package dev.rob.argus.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.concurrent.ArrayBlockingQueue;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public final class SwingRenderer implements Runnable {

    private final JFrame window = new JFrame("Client");

    private final ArrayBlockingQueue<BufferedImage> imageQueue;

    private final BufferStrategy strategy;

    public SwingRenderer(ArrayBlockingQueue<BufferedImage> imageQueue) {

        this.imageQueue = imageQueue;

        window.setDefaultCloseOperation(EXIT_ON_CLOSE);
        window.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        window.setLocationRelativeTo(null);

        var canvas = new Canvas();

        canvas.setIgnoreRepaint(true);
        canvas.setSize(window.getWidth(), window.getHeight());

        window.add(canvas);
        window.setVisible(true);

        canvas.createBufferStrategy(3);

        strategy = canvas.getBufferStrategy();
    }

    @Override
    public void run() {

        Graphics g = null;

        while (!Thread.currentThread().isInterrupted()) {

            try {

                var image = imageQueue.take();

                g = strategy.getDrawGraphics();

                g.drawImage(image, 0, 0, window.getWidth(), window.getHeight(), null);

                strategy.show();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            } finally { if (g != null) g.dispose(); }
        }
    }
}
