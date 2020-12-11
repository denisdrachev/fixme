package ru.market;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.market.model.Fix;
import ru.market.model.Instrument;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Slf4j
public class MarketClient {

    static final int PORT = 5001;
    static final String ADDRESS = "localhost";
    private ByteBuffer buffer = ByteBuffer.allocate(128);
    private String id = null;
    private List<Instrument> instruments = new ArrayList<>();
    private boolean sleep = false;

    private void run() throws Exception {

        initInstruments();

        System.out.println("Start!");

        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);//здесь объединяются селектор и канал (и вид активности: коннект)
        channel.connect(new InetSocketAddress(ADDRESS, PORT));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                StringBuffer stringBuffer = new StringBuffer();
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
                if (line.equals("t0")) {
                    System.out.println("\t" + "Activate No sleep");
                    sleep = false;
                    continue;
                } else if (line.equals("t10")) {
                    System.out.println("\t" + "Active Sleep 10 second");
                    sleep = true;
                    continue;
                }
                if (line.length() == 6) {
                    id = line;
                    stringBuffer.append(line);
                } else {
                    stringBuffer.append("49=").append(id).append("|").append(line);
                    String temp = stringBuffer.toString();
                    stringBuffer.append("|10=").append(getCheckSum(temp));
                    System.err.println(stringBuffer);
                }
                try {
                    queue.put(stringBuffer.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                SelectionKey key = channel.keyFor(selector);
                key.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }).start();

        while (true) {
            selector.select();
//            System.err.println("after selector.select()");
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isConnectable()) {
//                    System.out.println("selectionKey.isConnectable()");
                    channel.finishConnect();
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if (selectionKey.isReadable()) {
//                    System.out.println("selectionKey.isReadable()");
                    buffer.clear();
                    int numRead = channel.read(buffer);

                    if (id == null) {
                        id = new String(buffer.array()).trim();
                    }
                    log.info("Received: {}", new String(buffer.array()));
                    if (sleep) {
                        log.info("sleeping 10 sec");
                        Thread.sleep(10000);
                    }
                    try {
                        sendResponse(channel, numRead);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                } else if (selectionKey.isWritable()) {
//                    System.out.println("selectionKey.isWritable()");
                    String line = queue.poll();
                    if (line != null) {
                        log.info("Send: {}", line);
                        channel.write(ByteBuffer.wrap(line.getBytes()));
                    }
//                    if ("49=100000|54=1|1=bax|15=100|38=11|56=100000|10=304290585".equals(line)) {
//                        try {
//                            channel.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        System.exit(0);
//                    }
                    selectionKey.interestOps(SelectionKey.OP_READ);

                }
            }
            selector.selectedKeys().clear();
        }
    }

    @SneakyThrows
    private void sendResponse(SocketChannel channel, int numRead) {
        Fix fix = new Fix(buffer.array(), numRead);
        boolean success = false;
        for (Instrument instrument : instruments) {
            if (instrument.getName().equals(fix.getInstrument())) {
                if ("2".equals(fix.getDealType())) {
                    instrument.setCount(instrument.getCount() + fix.getCount());
                    success = true;
                } else if ("1".equals(fix.getDealType()) && instrument.getCount() >= fix.getCount()) {
                    instrument.setCount(instrument.getCount() - fix.getCount());
                    success = true;
                }
            }
        }
        if (success) {
            fix.setDealType("3");
            System.out.println("\n");
            instruments.forEach(instrument -> {
                System.out.println("\t\t" + instrument);
            });
            System.out.println("\n");
        } else {
            fix.setDealType("4");
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(fix.toString()).append("|")
                .append(StringUtil.CHECK_SUM).append("=").append(getCheckSum(fix.toString()));
        log.info("Send: {}", stringBuffer);
        channel.write(ByteBuffer.wrap(stringBuffer.toString().getBytes()));
    }

    private void initInstruments() {
        Instrument rub = new Instrument("RUB", 1000);
        Instrument eur = new Instrument("EUR", 1000);
        Instrument usd = new Instrument("USD", 1000);

        instruments.add(rub);
        instruments.add(eur);
        instruments.add(usd);
    }

    private String getCheckSum(String message) {
        byte bytes[] = message.getBytes();

        Checksum checksum = new CRC32();

        // update the current checksum with the specified array of bytes
        checksum.update(bytes, 0, bytes.length);

        // get the current checksum value
        long checksumValue = checksum.getValue();

        return String.valueOf(checksumValue);
    }

    public static void main(String[] args) throws Exception {
        new MarketClient().run();
    }
}
