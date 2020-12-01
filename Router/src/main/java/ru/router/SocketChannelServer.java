//package ru.router;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.SocketAddress;
//import java.net.URISyntaxException;
//import java.nio.ByteBuffer;
//import java.nio.channels.*;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//public class SocketChannelServer {
//    public static void main(String[] args) throws IOException, URISyntaxException {
//
//        SocketChannel server = SocketChannel.open();
//        SocketAddress socketAddr = new InetSocketAddress("localhost", 9000);
//        server.connect(socketAddr);
//
//        Path path = Paths.get("C:\\Users\\Ampersand\\Downloads\\matcha-master\\matcha-master\\fix-me\\Broker\\src\\main\\resources\\temp1.txt");
////        Path path = Paths.get(ClassLoader.getSystemResource("temp.txt").toURI());
//
//        FileChannel fileChannel = FileChannel.open(path);
//        ByteBuffer buffer = ByteBuffer.allocate(1024);
//        while(fileChannel.read(buffer) > 0) {
//            buffer.flip();
//            server.write(buffer);
//            buffer.clear();
//        }
//        fileChannel.close();
//        System.out.println("File Sent");
//        server.close();
//    }
//
//    public static void temp() throws IOException {
//
//        Selector selector = Selector.open();
//        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//        serverSocketChannel.configureBlocking(false);
//
//        SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_READ);
//
//                //ServerSocketChannel serverSocket = ServerSocketChannel.open();
//
//    }
//}
