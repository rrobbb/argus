package dev.rob.argus.server;

import dev.rob.argus.server.core.ScreenRecorder;
import dev.rob.argus.server.core.ScreenStreamer;
import dev.rob.argus.server.network.Server;
import dev.rob.argus.server.network.ServerTCP;
import org.junit.jupiter.api.Test;

class ServerTest {

    @Test
    public void TCP() throws InterruptedException {

        Server server = new ServerTCP(1234);

        server.start();

        Thread.currentThread().join();
    }

    @Test
    public void SRT() throws InterruptedException {

        var url = "srt://127.0.0.1:1234?mode=listener&latency=200000";

        new Thread(new ScreenStreamer(new ScreenRecorder(url, 60))).start();

        Thread.currentThread().join();
    }
}