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
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    private String id = null;
    private List<Instrument> instruments = new ArrayList<>();
    private boolean sleep = false;

    private void run() throws Exception {
        initInstruments();

        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);//здесь объединяются селектор и канал (и вид активности: коннект)
        channel.connect(new InetSocketAddress(ADDRESS, PORT));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                consoleHandle(channel, line);
            }
        }).start();

        while (true) {
            selector.select();
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isConnectable()) {
                    channel.finishConnect();
                    log.info("Connection success");
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if (selectionKey.isReadable()) {
                    readAndSend(channel);
                } else if (selectionKey.isWritable()) {
                    String line = queue.poll();
                    if (line != null) {
                        log.info("Send: {}", line);
                        channel.write(ByteBuffer.wrap(line.getBytes()));
                    }
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }
            }
            selector.selectedKeys().clear();
        }
    }

    private void readAndSend(SocketChannel channel) throws IOException {
        buffer.clear();

        int numRead = channel.read(buffer);
        log.info("Received: {}", new String(buffer.array()));
        if (numRead == 6) {
            if (id == null) {
                id = new String(buffer.array()).trim();
            }
        } else {
            byte[] dataCopy = new byte[numRead];
            System.arraycopy(buffer.array(), 0, dataCopy, 0, numRead);
            String inputString = new String(dataCopy);
            try {
                if (sleep) {
                    log.info("sleeping 10 sec");
                    Thread.sleep(10000);
                }
                sendResponse(channel, inputString);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
    }

    private void consoleHandle(SocketChannel channel, String line) {
        if ("q".equals(line)) {
            try {
                channel.close();
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
            System.exit(0);
        }
        if (line.equals("t0")) {
            log.info("Activate No sleep");
            sleep = false;
        } else if (line.equals("t10")) {
            log.info("Active Sleep 10 second");
            sleep = true;
        }
        if (line.length() == 6) {
            id = line;
            log.info("Set new ID: {}", id);
        }
    }

    @SneakyThrows
    private void sendResponse(SocketChannel channel, String inputString) {
        String[] inputSplit = inputString.split(" ");

        for (String input : inputSplit) {
            Fix fix = new Fix(input);
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
                System.out.println();
                instruments.forEach(instrument -> {
                    System.out.println("\t\t" + instrument);
                });
                System.out.println();
            } else {
                fix.setDealType("4");
            }
            fix.setCheckSum(getCheckSum(fix.toShortString()));
            log.info("Send: {}", fix);
            channel.write(ByteBuffer.wrap(fix.toString().getBytes()));
        }
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
        checksum.update(bytes, 0, bytes.length);
        long checksumValue = checksum.getValue();
        return String.valueOf(checksumValue);
    }

    public static void main(String[] args) throws Exception {
        new MarketClient().run();
    }
}
