package server.controller;

import common.*;
import server.Integration.DBHandler;
import server.model.User;
import server.net.SocketServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Controller extends UnicastRemoteObject implements FileServer {
    private DBHandler dbHandler;
    private SocketServer socketServer;
    FileDTO fileDTO;
    List<FileDTO> files;

    private final Map<Integer, User> loginUser = Collections.synchronizedMap(new HashMap<>());

    public Controller() throws RemoteException {
        this.dbHandler = new DBHandler();
        this.fileDTO = new FileDTO();
        this.files = new ArrayList<>();
        this.socketServer = new SocketServer();
        new Thread(this.socketServer).start();

    }

    @Override
    public synchronized void createUser(String username, String password, FileClient fileClient) throws RemoteException {
        User user = new User(username, password, fileClient);
        String msg = dbHandler.createUser(user);
        System.out.println(msg);
        if (fileClient == null) {
            System.out.println("file client is null");
        }
        fileClient.recvMsg(msg);


    }

    @Override
    public synchronized UserDTO loginUser(String username, String password, FileClient fileClient) {
        if (fileClient == null) {
            System.out.println("file client is null");
        }
        int uid = 0;
        User user = new User(username, password, fileClient);
        uid = dbHandler.loginUser(user);
        if (uid != 0) {
            user.setUid(uid);
            loginUser.put(user.getUid(), user);
            return user;
        }
        return null;
    }

    @Override
    public synchronized void logoutUser(UserDTO userDTO) throws RemoteException {
        User user = loginUser.get(userDTO.getUid());
        String username = loginUser.get(user.getUid()).getUsername();
        loginUser.get(user.getUid()).getResvmsg().recvMsg("Logout the account: " + username);
        loginUser.remove(user.getUid());

    }

    @Override
    public synchronized void showAllFiles(UserDTO userDTO) throws RemoteException {
        User user = loginUser.get(userDTO.getUid());
        loginUser.get(user.getUid()).getResvmsg().recvMsg(dbHandler.showFiles());

    }

    @Override
    public synchronized void uploadFile(FileDTO fileDTO, UserDTO userDTO) throws RemoteException {
        User user = loginUser.get(userDTO.getUid());
        loginUser.get(user.getUid()).getResvmsg().recvMsg(dbHandler.uploadFile(fileDTO, user.getUsername()));
    }

    @Override
    public synchronized void deleteFile(FileDTO fileDTO, UserDTO userDTO) throws RemoteException {
        User user = loginUser.get(userDTO.getUid());
        String fileowner = dbHandler.getFileowner(fileDTO);
        fileDTO.setFileowner(fileowner);
        boolean res = dbHandler.deleteFile(fileDTO, user.getUsername());
        if (res) {
            FileWorker fw = new FileWorker(fileDTO.getFileName());
            fw.deleteFile();
            if (fileowner != user.getUsername()) {
                User fileOwner = loginUser.get(dbHandler.getUserID(fileowner));
                if (fileOwner != null)
                    fileOwner.getResvmsg().recvMsg("Your file has been deleted by " + user.getUsername());
            }
            loginUser.get(user.getUid()).getResvmsg().recvMsg("File has been deleted.");
        } else {
            loginUser.get(user.getUid()).getResvmsg().recvMsg("Unable to delete the protected file.");
        }
    }

    @Override
    public synchronized void downloadFile(FileDTO fileDTO, UserDTO userDTO) throws RemoteException {

        User user = loginUser.get(userDTO.getUid());
        String fileOwner = dbHandler.getFileowner(fileDTO);
        if (fileOwner != user.getUsername()) {
            if (loginUser.get(fileOwner) != null) {
                loginUser.get(fileOwner).getResvmsg().recvMsg("Download file " + fileDTO.getFileName() + " by " + loginUser.get(user.getUid()).getUsername());
            }
        }

    }
}
