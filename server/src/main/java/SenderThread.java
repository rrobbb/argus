import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

public final class SenderThread extends Thread {

    private static final int PACKET_SIZE = 1400, FPS = 60;

    private final int PORT;

    private final BlockingQueue<byte[]> bytesQueue;

    private final InetAddress address;

    public SenderThread(BlockingQueue<byte[]> bytesQueue, InetAddress address, int port) {
        this.bytesQueue = bytesQueue;
        this.address = address;
        PORT = port;
    }

    @Override
    public void run() {

        try (final var socket = new DatagramSocket()) {

            socket.setSendBufferSize(4_000_000);

            int frameID = 0;

            final var packetBuffer = new byte[PACKET_SIZE + 8];

            var datagram = new DatagramPacket(packetBuffer, packetBuffer.length, address, PORT);

            final long ms = 1000L / FPS;

            long nextFrameTime = System.currentTimeMillis();

            while (!isInterrupted()) {

                long now = System.currentTimeMillis();

                byte[] data = bytesQueue.take();

                frameID++;

                if (now < nextFrameTime) Thread.sleep(nextFrameTime - now);

                nextFrameTime += ms;

                int total = (data.length + PACKET_SIZE - 1) / PACKET_SIZE;

                for (int i = 0; i < total; i++) {

                    int start = i * PACKET_SIZE;

                    int length = Math.min(PACKET_SIZE, data.length - start);

                    packetBuffer[0] = (byte) (frameID >> 24);
                    packetBuffer[1] = (byte) (frameID >> 16);
                    packetBuffer[2] = (byte) (frameID >> 8);
                    packetBuffer[3] = (byte) frameID;
                    packetBuffer[4] = (byte) (i >> 8);
                    packetBuffer[5] = (byte) i;
                    packetBuffer[6] = (byte) (total >> 8);
                    packetBuffer[7] = (byte) total;

                    System.arraycopy(data, start, packetBuffer, 8, length);

                    datagram.setData(packetBuffer, 0, length + 8);

                    socket.send(datagram);
                }
            }

        } catch (InterruptedException | IOException ignored) {}
    }
}