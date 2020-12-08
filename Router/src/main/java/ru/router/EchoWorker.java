package ru.router;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class EchoWorker implements Runnable {

    private final List<ServerDataEvent2> queue = new LinkedList();

    public void processData(NioServer server, SocketChannel socket, byte[] data, int count) {
        if (count == -1)
            return;
        System.out.println("COUNT: " + count);
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized (queue) {
            queue.add(new ServerDataEvent2(server, socket, dataCopy));
            queue.notify();
        }
    }


    @Override
    public void run() {
        ServerDataEvent2 dataEvent;
        while (true) {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Received = " + new String(queue.get(0).data));
                dataEvent = queue.remove(0);
            }
            if (dataEvent.data.length > 0)
                dataEvent.getServer().send(dataEvent.socket, dataEvent.data);
        }
    }


}
