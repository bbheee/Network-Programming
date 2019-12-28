package client.net;

import common.Constants;
import common.MessageSplitter;
import common.MsgType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Manages all communication with the server.
 */
public class ServerConnection implements Runnable {

    private final ByteBuffer msgFromServer = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private final MessageSplitter msgSplitter = new MessageSplitter();

    private InetSocketAddress serverAddress;
    private SocketChannel socketChannel;
    private Selector selector;
    private OutputHandler listener;
    private volatile boolean timeToSend = false;
    private volatile boolean connected;
    private String key;

    /**
     * The communicating thread, all communication is non-blocking. First, server connection is
     * established. Then the thread sends messages submitted via one of the <code>send</code>
     * methods in this class and receives messages from the server.
     */
    @Override
    public void run() {
        try {
            initConnection();
            initSelector();

            while (connected || !messagesToSend.isEmpty()) {
                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }

                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        completeConnection(key);
                    }
                    if (key.isReadable()) {
                        recvFromServer(key);
                    }
                    if (key.isWritable()) {
                        sendToServer(key);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        try {
            doDisconnect();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }


    /**
     * Starts the communicating thread and connects to the server.
     *
     * @param host Host name or IP address of server.
     * @param port Server's port number.
     * @throws IOException If failed to connect.
     */
    public void connect(String host, int port, OutputHandler outputHandler) {
        serverAddress = new InetSocketAddress(host, port);
        new Thread(this).start();
        listener = outputHandler;
    }

    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT); //set the key to OP_CONNECT
    }

    private void initConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;
    }

    private void completeConnection(SelectionKey key) throws IOException {
        if (socketChannel.finishConnect()) {
            Executor pool = ForkJoinPool.commonPool();
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.handleMsg("Login to play");

                }
            });
        }
    }

    public void doDisconnect() throws IOException {
        socketChannel.close();
        connected = false;
    }

    private void sendToServer(SelectionKey key) throws IOException {
        ByteBuffer msg;
        synchronized (messagesToSend) {
            while ((msg = messagesToSend.peek()) != null) {
                socketChannel.write(msg);
                if (msg.hasRemaining()) {
                    return;
                }
                messagesToSend.remove();
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void recvFromServer(SelectionKey key) throws IOException {
        msgFromServer.clear();
        int numOfReadBytes = socketChannel.read(msgFromServer);
        if (numOfReadBytes == -1) {
            throw new IOException("Lost connection");
        }
        String recvdString = extractMessageFromBuffer();
        msgSplitter.appendRecvdString(recvdString);
        while (msgSplitter.hasNext()) {
            String msg = msgSplitter.nextMsg();
            Executor pool = ForkJoinPool.commonPool();
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    listener.handleMsg(msg);
                }
            });
        }

    }

    private String extractMessageFromBuffer() {
        msgFromServer.flip();
        byte[] bytes = new byte[msgFromServer.remaining()];
        msgFromServer.get(bytes);
        return new String(bytes);
    }

    /**
     * Sends a start game entry to the server
     */
    public void sendStartGameEntry(String token) {
        try {
            sendMsg(MsgType.START_GAME.toString(), token);
        } catch (Throwable throwable) {
            System.err.println(throwable.getMessage());
        }
    }

    /**
     * Sends a guess entry to the server
     */
    public void sendGuessEntry(String token, String msg) {
        sendMsg(MsgType.GUESS.toString(), token, msg);
    }

    /**
     * Sends a login entry to the server
     */
    public void sendLogin(String username, String password) {
        try {
            sendMsg(MsgType.LOGIN.toString(), username + Constants.LOGIN_DELIMITER + password);
        } catch (Throwable throwable) {
            System.err.println(throwable.getMessage());
        }
    }

    private void sendMsg(String... parts) {
        StringJoiner joiner = new StringJoiner(Constants.MSG_TYPE_DELIMETER);
        for (String part : parts) {
            joiner.add(part);
        }
        String messageWithLengthHeader = MessageSplitter.prependLengthHeader(joiner.toString());
        synchronized (messagesToSend) {
            messagesToSend.add(ByteBuffer.wrap(messageWithLengthHeader.getBytes()));
        }
        timeToSend = true;
        selector.wakeup();

    }
}
