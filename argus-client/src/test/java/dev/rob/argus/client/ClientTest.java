package dev.rob.argus.client;

import dev.rob.argus.client.core.VideoDecoder;
import dev.rob.argus.client.core.VideoSource;
import dev.rob.argus.client.network.ClientTCP;
import dev.rob.argus.client.ui.SwingRenderer;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.concurrent.ArrayBlockingQueue;

class ClientTest {

    @Test
    void TCP() {

        var client = new ClientTCP("localhost", 1234);

        client.start();
    }

    @Test
    void SRT() throws InterruptedException {

        final var imageQueue = new ArrayBlockingQueue<BufferedImage>(1);

        new Thread(new VideoDecoder(new VideoSource("srt://127.0.0.1:1234?mode=caller&latency=200000"), imageQueue)).start();

        new Thread(new SwingRenderer(imageQueue)).start();

        Thread.currentThread().join();
    }
}