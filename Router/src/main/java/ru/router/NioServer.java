package ru.router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NioServer /*implements Runnable*/ {

    public static void main(String[] args) {
        SpringApplication.run(NioServer.class, args);
    }

    /*private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private EchoWorker worker = new EchoWorker();
    private final List<ChangeRequest> changeRequests = new LinkedList();

    public static int index = 100000;
    private final Chain chain;

    @Getter
    private static Map<String, SocketChannel> channelMap = new ConcurrentHashMap<>();

    private NioServer(String address, int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress isa = new InetSocketAddress(address, port);
        serverChannel.socket().bind(isa);
        selector = SelectorProvider.provider().openSelector();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        Transactor transactor = new Transactor();
        Sider sider = new Sider();
        Sender sender = new Sender();

        chain = new Validator();
        chain.setNext(sider);
        chain.getNext().setNext(transactor);
        chain.getNext().getNext().setNext(sender);

        new Thread(worker).start();
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NioServer(ADDRESS, PORT_BROKER)).start();
//        new Thread(new NioServer(ADDRESS, PORT_MARKET)).start();
        new NioServer(ADDRESS, PORT_MARKET).run();
    }

    @SneakyThrows
    public void run() {
        while (true) {
            synchronized (changeRequests) {
                for (ChangeRequest change : changeRequests) {
                    switch (change.type) {
                        case CHANGEOPS:
                            SelectionKey key = change.channel.keyFor(selector);
                            key.interestOps(change.ops);
                            break;
                        default:
                    }
                }
                changeRequests.clear();
            }
            selector.select();
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    System.out.println("key.isWritable() WOW");
//                    write(key);
                }
            }
        }
    }
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.write(ByteBuffer.wrap(String.valueOf(index).getBytes()));
        channelMap.put(String.valueOf(index), socketChannel);
        index++;
    }

    @SneakyThrows
    private void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        readBuffer.clear();
        int numRead = socketChannel.read(readBuffer);

        try {
            Fix fix = new Fix(readBuffer.array(), numRead);
            System.err.println(fix);
            chain.handle(fix);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
*//*
    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        synchronized (pendingData) {
            List<ByteBuffer> queue = pendingData.get(socketChannel);
            while (!queue.isEmpty()) {
                ByteBuffer buf = queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    break;
                }
                System.out.println("Send echo = " + new String(buf.array()));
                queue.remove(0);
            }
            if (queue.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }*//*
*//*
    public void send(SocketChannel socket, byte[] data) {
        synchronized (changeRequests) {
            changeRequests.add(new ChangeRequest(socket, CHANGEOPS, OP_WRITE));
            synchronized (pendingData) {
                List<ByteBuffer> queue = pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList<>();
                    pendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }
        selector.wakeup();
    }*/
}