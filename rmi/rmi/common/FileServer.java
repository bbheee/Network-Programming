package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileServer extends Remote {

    public UserDTO loginUser(String username, String password, FileClient fileClient) throws RemoteException;

    public void createUser(String username, String password, FileClient fileClient) throws RemoteException;

    public void logoutUser(UserDTO userDTO) throws RemoteException;

    public void showAllFiles(UserDTO userDTO) throws RemoteException;

    public void uploadFile(FileDTO fileDTO, UserDTO userDTO) throws RemoteException;

    public void downloadFile(FileDTO fileDTO, UserDTO userDTO) throws RemoteException;

    public void deleteFile(FileDTO fileDTO, UserDTO userDTO) throws RemoteException;
}