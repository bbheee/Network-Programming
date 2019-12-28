package client.net;

import common.Command;
import common.FileWorker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ServerConnection {

    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;

    static {
        FileWorker.setGlobalPath("/Users/beibei/Documents/NetworkProgramming/rmi/clientStorage/");
    }

    private Socket socket;
    private FileWorker fileWorker;

    public ServerConnection() {
    }

    public void sendFile(FileWorker fileWorker) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 8081), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);

        // Send file
        fileWorker.sendFile(socket.getOutputStream());
        socket.close();
    }

    public void sendFile(String fileName) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 8081), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);

        // Read File
        fileWorker = new FileWorker(fileName);
        if (fileWorker.fileExists()) {
            fileWorker.readFile();
        } else {
            System.err.println("File doesn't exist.");
            socket.close();
            return;
        }

        // Send file
        byte[] command = Command.UPLOAD.toString().getBytes();
        byte[] length = ByteBuffer.allocate(Integer.BYTES).putInt(command.length).array();
        socket.getOutputStream().write(length);
        socket.getOutputStream().write(command);
        fileWorker.sendFile(socket.getOutputStream());
        socket.close();
    }

    public void receiveFile(String fileName) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 8081), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);

        byte[] command = Command.DOWNLOAD.toString().getBytes();
        byte[] length = ByteBuffer.allocate(Integer.BYTES).putInt(command.length).array();
        socket.getOutputStream().write(length);
        socket.getOutputStream().write(command);

        byte[] fileNameBytes = fileName.getBytes();
        byte[] nameLength = ByteBuffer.allocate(Integer.BYTES).putInt(fileNameBytes.length).array();
        socket.getOutputStream().write(nameLength);
        socket.getOutputStream().write(fileNameBytes);

        socket.getOutputStream().flush();

        fileWorker = new FileWorker();
        fileWorker.receiveFile(socket.getInputStream());
        if (fileWorker.fileExists()) {
            System.err.println("File already exists.");
        } else {
            fileWorker.writeFile();
            System.out.println("File has been downloaded now.");
        }
        socket.close();
    }

}