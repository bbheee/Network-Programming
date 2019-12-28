package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileClient extends Remote {
    /**
     * The specified message is received by the client.
     *
     * @param msg The message that shall be received.
     */
    void recvMsg(String msg) throws RemoteException;

}
