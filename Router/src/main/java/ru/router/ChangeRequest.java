package ru.router;

import java.nio.channels.SocketChannel;

public class ChangeRequest {

    public static final int CHANGEOPS = 1;

    SocketChannel channel;
    public int type;
    int ops;

    public ChangeRequest(SocketChannel channel, int type, int ops) {
        this.channel = channel;
        this.type = type;
        this.ops = ops;
    }
}
