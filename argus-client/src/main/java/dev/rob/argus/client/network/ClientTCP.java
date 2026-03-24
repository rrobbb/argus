package dev.rob.argus.client.network;

import dev.rob.argus.client.ui.SwingRenderer;

import dev.rob.argus.client.core.VideoSource;
import dev.rob.argus.client.core.VideoDecoder;

import java.awt.image.BufferedImage;

import java.io.IOException;

import java.net.Socket;

import java.util.concurrent.ArrayBlockingQueue;

public final class ClientTCP {

    private final Socket socket;

    public ClientTCP(String address, int port) {

        try { socket = new Socket(address, port); } catch (IOException e) { throw new RuntimeException(e); }

    }

    public void start() {

        final var imageQueue = new ArrayBlockingQueue<BufferedImage>(1);

        try (socket) {

            new Thread(new VideoDecoder(new VideoSource(socket.getInputStream()), imageQueue)).start();

            new Thread(new SwingRenderer(imageQueue)).start();

            Thread.currentThread().join();

        } catch (IOException | InterruptedException e) { throw new RuntimeException(e); }

        System.out.println("Socket closed.");
    }
}
