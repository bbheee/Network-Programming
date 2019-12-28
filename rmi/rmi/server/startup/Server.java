package server.startup;

import server.controller.Controller;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Starts the file servant and binds it in the RMI registry.
 */
public class Server {
    /**
     * @param args There are no command line arguments.
     */
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.startRegistry();
        } catch (MalformedURLException | RemoteException ex) {
            System.out.println("Could not start chat server.");
        }
    }

    private void startRegistry() throws RemoteException, MalformedURLException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException noRegistryIsRunning) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
        Controller controller = new Controller();
        Naming.rebind("rmi", controller);
    }
}
