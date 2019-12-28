package server.net;

import common.MessageException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Receives chat messages and broadcasts them to all chat clients. All communication to/from any
 * chat node pass this server.
 */
public class GameServer {
    private static final int LINGER_TIME = 5000;
    private int portNo = 8080;
    private Selector selector;
    private ServerSocketChannel listeningSocketChannel;

    private static ArrayList<String> wordList = new ArrayList<>();

    /**
     * @param args Takes one command line argument, the number of the port on which the server will
     *             listen, the default is <code>8080</code>.
     */
    public static void main(String[] args) {
        //read the file in a separate thread
        CompletableFuture.runAsync(() -> {
            //file read with ByteChannel
            try {
                Path file = Paths.get(args[0]);
                SeekableByteChannel fileChannel = Files.newByteChannel(file);
                ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());

                while (fileChannel.read(buffer) >= 0) {
                    buffer.flip();
                    for (int i = 0; i < fileChannel.size(); i++) {
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        String str = new String(bytes);
                        List list = str.lines()
                                .map(word -> word.trim())
                                .collect(Collectors.toList());
                        wordList.addAll(list);
                    }
                    buffer.clear();
                }

            } catch (Throwable e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        });

        // Start the server
        GameServer server = new GameServer();
        server.serve();
    }

    private void serve() {
        try {
            initSelector();
            initListeningSocketChannel();
            System.out.println("Starting to serve on port " + portNo);


            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        startHandler(key);//startHandler run for the person who connected
                    } else if (key.isReadable()) {
                        recvFromClient(key);
                    } else if (key.isWritable()) {
                        sendToClient(key);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Server failure.");
        }

    }

    private void startHandler(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        System.out.println("Receiving new client on port " + clientChannel.getLocalAddress());
        ClientHandler client = new ClientHandler(this, clientChannel, wordList);
        clientChannel.register(selector, SelectionKey.OP_READ, client);
        clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
    }

    private void recvFromClient(SelectionKey key) throws IOException {
        ClientHandler client = (ClientHandler) key.attachment();
        try {
            client.recvMsg();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendToClient(SelectionKey key) throws IOException {
        ClientHandler client = (ClientHandler) key.attachment();
        try {
            client.sendMsg();
        } catch (MessageException e) {
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void initSelector() throws IOException {
        selector = Selector.open();
    }

    private void initListeningSocketChannel() throws IOException {
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(portNo));
        listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Switch the selector of clientChannel to OP_WRITE
     */
    public void send(SocketChannel clientChannel) throws ClosedChannelException {
        clientChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    /**
     * Switch the selector of clientChannel to OP_READ
     */
    public void receive(SocketChannel clientChannel) throws ClosedChannelException {
        clientChannel.keyFor(selector).interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

}
