import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Renderer extends JFrame {

    private final BufferStrategy strategy;

    public Renderer() {

        setTitle("Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setLocationRelativeTo(null);

        var canvas = new Canvas();

        canvas.setIgnoreRepaint(true);
        canvas.setSize(getWidth(), getHeight());

        add(canvas);

        setVisible(true);

        canvas.createBufferStrategy(3);

        strategy = canvas.getBufferStrategy();
    }

    public synchronized void display(BufferedImage image) {

        if (image == null || strategy == null) return;

        var g = strategy.getDrawGraphics();

        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.dispose();

        strategy.show();
    }
}
