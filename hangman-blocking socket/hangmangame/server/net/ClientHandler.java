package server.net;

import common.Constants;
import common.IO;
import common.MsgType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import server.controller.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all communication with one game client.
 */
class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private OutputStream out;
    private InputStream in;
    private Controller controller;
    private String username = "hello";
    private String password = "123";
    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);


    /**
     * Creates a new instance, which will handle communication with one specific client connected to
     * the specified socket.
     *
     * @param clientSocket The socket to which this handler's client is connected.
     */
    ClientHandler(Socket clientSocket, ArrayList<String> words) {
        this.clientSocket = clientSocket;//represents one connection between the server and a client
        this.controller = new Controller(words);
    }

    /**
     * The run loop handling all communication with the connected client.
     */
    @Override
    public void run() {
        try {
            out = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        while (clientSocket.isConnected()) {
            try {
                String[] inputArray;
                String msg = IO.receive(in);
                try {
                    inputArray = msg.split(Constants.MSG_DELIMETER);
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
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }

        }
    }

    private void sendGameState(String[] msg) {
        List<String> body = Arrays.asList(msg);
        String message = body.stream()
                .map(state -> state + ":")
                .collect(Collectors.joining());
        IO.send(out, MsgType.STATE.toString() + Constants.MSG_DELIMETER + message);
    }

    private void sendToken(String jws) {
        IO.send(out, MsgType.LOGIN.toString() + Constants.MSG_DELIMETER + jws);
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

    private void disconnectClient() {
        try {
            clientSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
