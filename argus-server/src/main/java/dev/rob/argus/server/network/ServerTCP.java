package dev.rob.argus.server.network;

import dev.rob.argus.server.core.ScreenRecorder;
import dev.rob.argus.server.core.ScreenStreamer;

import java.io.IOException;

import java.net.ServerSocket;

public final class ServerTCP implements Server {

    private final int port;

    public ServerTCP(int port) { this.port = port; }

    public void start() {

        try (final var server = new ServerSocket(port, 50)) {

            System.out.println("Waiting for a client.");

            final var client = server.accept();

            client.setTcpNoDelay(true);

            System.out.println("Client connected: " + client.getInetAddress().getHostAddress());

            new Thread(new ScreenStreamer(new ScreenRecorder(client.getOutputStream(), DEFAULT_FRAMERATE))).start();

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }
}