import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

public final class SenderThread extends Thread {

    private static final int PAYLOAD_SIZE = 1400;

    private static final int HEADER_SIZE = 12;

    private static final int PACKET_SIZE = PAYLOAD_SIZE + HEADER_SIZE;

    private final int port;

    private final BlockingQueue<byte[]> bytesQueue;

    private final InetAddress address;

    public SenderThread(BlockingQueue<byte[]> bytesQueue, InetAddress address, int port) {
        this.bytesQueue = bytesQueue;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {

        byte[] data, latestData;

        try (final var socket = new DatagramSocket()) {

            socket.setSendBufferSize(4_000_000);

            int frameID = 0;

            final var packetBuffer = new byte[PACKET_SIZE];

            var datagram = new DatagramPacket(packetBuffer, packetBuffer.length, address, port);

            while (!isInterrupted()) {

                data = bytesQueue.take();

                latestData = bytesQueue.poll();

                while (latestData != null) {
                    data = latestData;
                    latestData = bytesQueue.poll();
                }

                frameID++;

                int totalSizeBytes = data.length;

                int totalChunks = (data.length + PAYLOAD_SIZE - 1) / PAYLOAD_SIZE;

                for (int i = 0; i < totalChunks; i++) {

                    int start = i * PAYLOAD_SIZE;

                    int length = Math.min(PAYLOAD_SIZE, data.length - start);

                    // Header: FRAME ID (4 bytes)
                    packetBuffer[0] = (byte) (frameID >> 24);
                    packetBuffer[1] = (byte) (frameID >> 16);
                    packetBuffer[2] = (byte) (frameID >> 8);
                    packetBuffer[3] = (byte) frameID;

                    // Header: CHUNK INDEX (2 bytes)
                    packetBuffer[4] = (byte) (i >> 8);
                    packetBuffer[5] = (byte) i;

                    // Header: TOTAL CHUNK (2 bytes)
                    packetBuffer[6] = (byte) (totalChunks >> 8);
                    packetBuffer[7] = (byte) totalChunks;

                    // Header: TOTAL SIZE IN BYTES (4 bytes)
                    packetBuffer[8] = (byte) (totalSizeBytes >> 24);
                    packetBuffer[9] = (byte) (totalSizeBytes >> 16);
                    packetBuffer[10] = (byte) (totalSizeBytes >> 8);
                    packetBuffer[11] = (byte) totalSizeBytes;

                    System.arraycopy(data, start, packetBuffer, HEADER_SIZE, length);

                    datagram.setData(packetBuffer, 0, length + HEADER_SIZE);

                    socket.send(datagram);
                }
            }

        } catch (InterruptedException | IOException ignored) {}
    }
}