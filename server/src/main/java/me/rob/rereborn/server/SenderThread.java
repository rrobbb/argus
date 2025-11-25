package me.rob.rereborn.server;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.OutputStream;

public final class SenderThread extends Thread {

    private final PipedInputStream in;

    private final OutputStream out;

    public SenderThread(PipedInputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        final var buffer = new byte[4096];

        int bytesRead;

        try {

            while (!isInterrupted() && (bytesRead = in.read(buffer)) != -1)
                if (bytesRead > 0) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }

        } catch (IOException e) {

            System.err.println(e.getMessage());

        } finally {

            try {

                in.close();

                out.close();

            } catch (IOException ignored) {}

            Thread.currentThread().interrupt();
        }
    }
}