package server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer implements Runnable {

    private static final int LINGER = 5000;
    private static final int TIMEOUT = 1800000;
    private static int PORT_NO = 8081;

    @Override
    public void run() {
        try {
            ServerSocket listenSocket = new ServerSocket(PORT_NO);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                clientSocket.setSoLinger(true, LINGER);
                clientSocket.setSoTimeout(TIMEOUT);
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
}