package server.net;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Game server looks for clients to join and creates a ClientHandler for each
 */
public class GameServer {
    private static final int LINGER_TIME = 5000;
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private int portNo = 8080;
    private static ArrayList<String> wordList = new ArrayList<>();

    /**
     * @param args Takes one command line argument, the number of the port on which the server will
     *             listen, the default is <code>8080</code>.
     */
    public static void main(String[] args) {
        try {
            FileReader fileReader = new FileReader(args[0]);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List list = bufferedReader.lines()
                    .map(word -> word.trim())
                    .collect(Collectors.toList());
            wordList.addAll(list);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // Start the server
        GameServer server = new GameServer();
        server.serve();
    }


    private void serve() {
        try {
            System.out.println("Starting to serve on port " + portNo);
            ServerSocket listeningSocket = new ServerSocket(portNo);
            while (true) {
                Socket clientSocket = listeningSocket.accept();//look for people who wants to connect
                startHandler(clientSocket);//startHandler run for the person who connected
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void startHandler(Socket clientSocket) throws SocketException { //set up and starts a new clientHandler for the client with the socket clientSocket
        System.out.println("Receiving new client on port " + clientSocket.getLocalPort());
        clientSocket.setSoLinger(true, LINGER_TIME);
        clientSocket.setSoTimeout(TIMEOUT_HALF_HOUR);
        ClientHandler handler = new ClientHandler(clientSocket, wordList);

        Thread handlerThread = new Thread(handler);
        handlerThread.setPriority(Thread.MAX_PRIORITY);
        handlerThread.start();
    }
}
