package server.net;

import common.Command;
import common.FileWorker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientHandler implements Runnable {

    private Socket socket;

    static {
        FileWorker.setGlobalPath("/Users/beibei/Documents/NetworkProgramming/rmi/serverStorage/");
    }

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            InputStream inputStream = socket.getInputStream();
            byte[] data = readData(inputStream);
            String message = new String(data);
            Command command = Command.valueOf(message.toUpperCase());
            switch (command) {
                case UPLOAD:
                    receive(socket.getInputStream());
                    break;
                case DOWNLOAD:
                    data = readData(inputStream);
                    String fileName = new String(data);
                    send(socket.getOutputStream(), fileName);
                    break;
                default:
                    System.err.println("Not implemented over socket");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Shutting down connection...");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private byte[] readData(InputStream in) throws IOException {
        byte[] length = in.readNBytes(Integer.BYTES);
        int usernameLength = ByteBuffer.wrap(length).getInt();
        byte[] data = new byte[usernameLength];
        in.read(data);
        return data;
    }

    private void receive(InputStream in) {
        FileWorker fw = new FileWorker();
        fw.receiveFile(in);
        if (fw.fileExists()) {
            fw.deleteFile();
        }
        fw.writeFile();
    }

    private void send(OutputStream out, String fileName) {
        FileWorker fw = new FileWorker(fileName);
        if (!fw.fileExists()) {
            System.err.println("File doesn't exists");
            return;
        }
        fw.readFile();
        fw.sendFile(out);
    }
}

