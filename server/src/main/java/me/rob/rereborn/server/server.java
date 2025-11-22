import me.rob.rereborn.server.EncoderThread;
import me.rob.rereborn.server.SenderThread;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;

void main(String[] args) {

    if (args.length < 2) {
        System.err.println("Use: <address> <port> <fps>");
        System.exit(1);
    }

    final var PORT = Integer.parseInt(args[0]);

    final var FPS = Integer.parseInt(args[1]);

    try (final var serverSocket = new ServerSocket(PORT, 50)) {

        System.out.println("Waiting...");

        final var clientSocket = serverSocket.accept();

        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

        final var out = new PipedOutputStream();

        final var in = new PipedInputStream(out);

        new EncoderThread(out, FPS).start();

        new SenderThread(in, clientSocket).start();

    } catch (IOException e) {

        System.err.println(e.getMessage());
    }
}
