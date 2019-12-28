package client.net;

import common.Constants;
import common.IO;
import common.MsgType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringJoiner;

/**
 * Manages all communication with the server.
 */
public class ServerConnection {
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private volatile boolean connected;

    /**
     * Creates a new instance and connects to the specified server. Also starts a listener thread
     * receiving broadcast messages from server.
     *
     * @param host             Host name or IP address of server.
     * @param port             Server's port number.
     * @param broadcastHandler Called whenever a broadcast is received from server.
     * @throws IOException If failed to connect.
     */
    public void connect(String host, int port, OutputHandler broadcastHandler) throws
            IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);
        connected = true;
        out = socket.getOutputStream();
        in = socket.getInputStream();
        new Thread(new Listener(broadcastHandler)).start();
    }

    /**
     * Closes the connection with the server and stops the broadcast listener thread.
     *
     * @throws IOException If failed to close socket.
     */
    public void disconnect() throws IOException {
        sendMsg(MsgType.DISCONNECT.toString());
        socket.close();
        socket = null;
        connected = false;
    }

    /**
     * Sends a start game entry to the server
     */
    public void sendStartGameEntry(String token) {
        try {
            sendMsg(MsgType.START_GAME.toString(), token);
        } catch (Throwable throwable) {
            System.err.println(throwable.getMessage());
            throw throwable;
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
            throw throwable;
        }
    }

    private void sendMsg(String... parts) {
        StringJoiner joiner = new StringJoiner(Constants.MSG_DELIMETER);
        for (String part : parts) {
            if (part != null)
                joiner.add(part);
        }
        IO.send(out, joiner.toString());
    }

    private class Listener implements Runnable {
        private final OutputHandler outputHandler;

        private Listener(OutputHandler outputHandler) {
            this.outputHandler = outputHandler;
        }

        @Override
        public void run() {
            try {
                for (; ; ) {
                    outputHandler.handleMsg(extractMsgBody(IO.receive(in)));
                }
            } catch (Throwable connectionFailure) {
                if (connected) {
                    outputHandler.handleMsg("Lost connection.");
                }
            }
        }

        private String extractMsgBody(String entireMsg) {
            return entireMsg;
        }
    }
}
