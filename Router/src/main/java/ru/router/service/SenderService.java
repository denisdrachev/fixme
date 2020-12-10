//package ru.router.service;
//
//import org.springframework.stereotype.Service;
//import ru.router.ServerDataEvent;
//
//import java.nio.channels.SocketChannel;
//import java.util.LinkedList;
//import java.util.List;
//
////@Service
//public class SenderService implements Runnable {
//
//    public SenderService() {
//        System.out.println("SenderService start...");
////        new Thread(this).run();
//        System.out.println("SenderService started");
//    }
//
//    private final List<ServerDataEvent> queue = new LinkedList();
//
//    public void processData(IOInterface server, SocketChannel socket, byte[] data, int count) {
//        if (count == -1)
//            return;
//        System.out.println("COUNT: " + count);
//        byte[] dataCopy = new byte[count];
//        System.arraycopy(data, 0, dataCopy, 0, count);
//        synchronized (queue) {
//            queue.add(new ServerDataEvent(server, socket, dataCopy));
//            queue.notify();
//        }
//    }
//
//
//    @Override
//    public void run() {
//        System.err.println("1");
//        ServerDataEvent dataEvent;
//        while (true) {
//            synchronized (queue) {
//                while (queue.isEmpty()) {
//                    try {
//                        queue.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                System.out.println("Received = " + new String(queue.get(0).data));
//                dataEvent = queue.remove(0);
//            }
//            if (dataEvent.data.length > 0)
//                dataEvent.getServer().send(dataEvent.socket, dataEvent.data);
//        }
//    }
//
//
//}
