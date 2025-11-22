import me.rob.rereborn.client.DecoderThread;
import me.rob.rereborn.client.RendererThread;

import java.awt.image.BufferedImage;

void main(String[] args) throws IOException, InterruptedException {

    final var address = args[0];

    final var port = Integer.parseInt(args[1]);

    try (final var socket = new Socket(address, port)) {

        var imageQueue = new ArrayBlockingQueue<BufferedImage>(1);

        new DecoderThread(socket.getInputStream(), imageQueue).start();

        new RendererThread(imageQueue).start();

        Thread.currentThread().join();
    }

    System.out.println("Socket closed.");
}
