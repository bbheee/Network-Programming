package server.net;

import common.Constants;
import common.MessageSplitter;
import common.MsgType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import server.controller.Controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all communication with one game client.
 */
class ClientHandler {
    private Controller controller;
    private String username = "hello";
    private String password = "123";
    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    private final GameServer server;
    private final SocketChannel clientChannel;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private final MessageSplitter msgSplitter = new MessageSplitter();
    private final Queue<ByteBuffer> sendToClient = new ArrayDeque<>();

    /**
     * Creates a new instance, which will handle communication with one specific client connected to
     * the specified channel.
     *
     * @param clientChannel The socket to which this handler's client is connected.
     */
    ClientHandler(GameServer server, SocketChannel clientChannel, ArrayList<String> words) {
        this.server = server;
        this.clientChannel = clientChannel;
        this.controller = new Controller(words);
    }


    /**
     * The run loop handling all communication with the connected client.
     */
    public void run() {
        try {
            while (msgSplitter.hasNext()) {
                String[] inputArray;
                String msg = msgSplitter.nextMsg();  // msg from client

                try {
                    inputArray = msg.split(Constants.MSG_TYPE_DELIMETER);
                } catch (Throwable e) {
                    continue;
                }

                switch (MsgType.valueOf(inputArray[Constants.MSG_TYPE_INDEX].toUpperCase())) {
                    case LOGIN:
                        if (inputArray.length > Constants.MSG_JWT_LOGIN_INDEX &&
                                verifyUser(inputArray[Constants.MSG_JWT_LOGIN_INDEX])) {
                            String jws = createToken(inputArray[Constants.MSG_JWT_LOGIN_INDEX]);
                            sendToken(jws);
                        }
                        break;

                    case START_GAME:
                        if (verifyToken(username, inputArray[Constants.MSG_JWT_LOGIN_INDEX])) {
                            controller.startGame();
                            sendGameState(controller.getGameState());
                        }
                        break;

                    case GUESS:
                        if (inputArray.length > Constants.MSG_BODY_INDEX &&
                                verifyToken(username, inputArray[Constants.MSG_JWT_LOGIN_INDEX])) {
                            controller.guess(inputArray[Constants.MSG_BODY_INDEX]);
                            sendGameState(controller.getGameState());
                        }
                        break;

                    default:
                        disconnectClient();
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }


    public void sendMsg() throws IOException {
        ByteBuffer msg;
        synchronized (sendToClient) {
            while ((msg = sendToClient.peek()) != null) {
                clientChannel.write(msg);
                if (msg.hasRemaining()) {
                    return;
                }
                sendToClient.remove();

            }
        }
        server.receive(clientChannel);
    }

    /**
     * Reads a message from the connected client, then submits a task to the default
     * <code>ForkJoinPool</code>. That task which will handle the received message.
     *
     * @throws IOException If failed to read message
     */

    void recvMsg() throws IOException {

        msgFromClient.clear();
        int numOfReadBytes;
        numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        String recvdString = extractMessageFromBuffer();
        msgSplitter.appendRecvdString(recvdString);
        run();
    }

    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }

    private void sendGameState(String[] msg) throws IOException {
        List<String> body = Arrays.asList(msg);
        String message = body.stream()
                .map(state -> state + ":")
                .collect(Collectors.joining());
        msgToClient(MsgType.STATE, message);
    }

    private void sendToken(String jws) throws IOException {
        msgToClient(MsgType.LOGIN, jws);
    }


    private boolean verifyUser(String credentials) {
        String[] splitCreds = credentials.split(Constants.LOGIN_DELIMITER);
        try {
            String username = splitCreds[0];
            String password = splitCreds[1];
            if (this.username.equals(username.toLowerCase()) &&
                    this.password.equals(password)) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean verifyToken(String username, String jws) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(jws)
                    .getBody()
                    .getSubject()
                    .equals(username);
        } catch (Exception e) {
            System.err.println("Invalid JWT signature: " + e.getMessage());
            return false;
        }
    }

    private String createToken(String username) {
        String[] credentials = username.split(Constants.LOGIN_DELIMITER);
        return Jwts.builder().setSubject(credentials[0]).signWith(key).compact();

    }

    void disconnectClient() throws IOException {
        clientChannel.close();
    }

    private void msgToClient(MsgType msgType, String msg) throws IOException {
        StringBuilder str = new StringBuilder();

        str.append(msgType.toString());
        str.append(Constants.MSG_TYPE_DELIMETER);
        if (msg != null) {
            str.append(msg);
        }
        int length = str.toString().length();
        StringBuilder complete = new StringBuilder();
        complete.append(length);
        complete.append(Constants.MSG_LEN_DELIMETER);
        complete.append(str.toString());
        sendToClient.add(ByteBuffer.wrap(complete.toString().getBytes()));
        server.send(clientChannel);
    }
}
