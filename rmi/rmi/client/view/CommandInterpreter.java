package client.view;

import client.net.ServerConnection;
import common.*;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 * Reads and interprets user commands. The command interpreter will run in a separate thread, which
 * is started by calling the <code>start</code> method. Commands are executed in a thread pool, a
 * new prompt will be displayed as soon as a command is submitted to the pool, without waiting for
 * command execution to complete.
 */
public class CommandInterpreter implements Runnable {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCmds = false;
    private final ThreadSafeStdOut outMgr = new ThreadSafeStdOut();
    private FileServer fileServer;
    private FileDTO fileDTO;
    private UserDTO userDTO;
    private boolean isLogin;
    private ServerConnection serverConnection;
    private FileClient fileClient;


    public void start(FileServer fileServer) {
        this.fileServer = fileServer;
        this.serverConnection = new ServerConnection();
        receivingCmds = true;
        new Thread(this).start();
    }

    /**
     * Interprets and performs user commands.
     */
    @Override
    public void run() {
        System.out.println("Welcome to File Catalog!");
        System.out.println("Type login or register to start:");

        try {
            fileClient = new ConsoleOutput();

        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        while (receivingCmds) {

            try {
                while (!isLogin) {
                    String input = readNextLine();
                    String[] command = getCommand(input);
                    Command cmd = Command.valueOf(command[0].toUpperCase());

                    switch (cmd) {
                        case LOGIN:
                            userDTO = fileServer.loginUser(command[1], command[2], fileClient);
                            if (userDTO != null) {
                                isLogin = true;
                                System.out.println("Logged in as \"" + command[1] + "\"");
                            } else {
                                System.out.println("Register an account if you don't have one!");
                            }
                            break;
                        case REGISTER:
                            fileServer.createUser(command[1], command[2], fileClient);
                            break;
                        case LOGOUT:
                            System.out.println("You have logged out now!");
                            break;
                        default:
                            System.out.println("Something is wrong!");
                            continue;
                    }


                }
                while (isLogin) {
                    String input = readNextLine();
                    String[] command = getCommand(input);
                    Command cmd = Command.valueOf(command[0].toUpperCase());
                    switch (cmd) {
                        case LOGOUT:
                            fileServer.logoutUser(userDTO);
                            break;
                        case LISTFILE:
                            System.out.println("Here are the list of files:");
                            fileServer.showAllFiles(userDTO);
                            break;
                        case UPLOAD:
                            if (command.length > 1) {
                                FileWorker fw = new FileWorker(command[1]);
                                fw.readFile();
                                if (command.length > 2 && command[2].toLowerCase().equals("writeable")) {
                                    fileDTO = setFile(fw, true);
                                } else {
                                    fileDTO = setFile(fw, false);
                                }
                                try {
                                    fileServer.uploadFile(fileDTO, userDTO);
                                    serverConnection.sendFile(fileDTO.getFileName());
                                } catch (FileNotFoundException ex) {
                                    System.err.println(ex);
                                }
                            }
                            break;
                        case DOWNLOAD:
                            if (command.length > 1) {
                                fileDTO = new FileDTO();
                                fileDTO.setFileName(command[1]);
                                try {
                                    fileServer.downloadFile(fileDTO, userDTO);
                                    serverConnection.receiveFile(command[1]);
                                } catch (FileNotFoundException ex) {
                                    System.err.println(ex);
                                }
                            }
                            break;
                        case DELETE:
                            fileDTO = new FileDTO();
                            fileDTO.setFileName(command[1]);
                            fileServer.deleteFile(fileDTO, userDTO);
                            break;
                        default:
                            continue;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                outMgr.println("Operation failed");
            }
        }

    }

    private String readNextLine() {
        outMgr.print(PROMPT);
        return console.nextLine();
    }

    private FileDTO setFile(FileWorker fileWorker, boolean writeable) {
        return fileDTO = new FileDTO()
                .withFileName(fileWorker.getFileName())
                .withFileSize(fileWorker.getFileSize())
                .withFileOwner(userDTO.getUsername())
                .withWriteable(writeable);
    }


    public class ConsoleOutput extends UnicastRemoteObject implements FileClient {

        public ConsoleOutput() throws RemoteException {

        }

        @Override
        public void recvMsg(String msg) throws RemoteException {
            System.out.println(msg);
        }

    }

    private String[] getCommand(String input) {
        String[] cmd = input.split(" ");
        return cmd;
    }

}
