package client.startup;

import client.view.CommandInterpreter;
import common.FileServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Main {

    public static void main(String[] args) {
        try {
            FileServer fileServer = (FileServer) Naming.lookup("rmi");
            new CommandInterpreter().start(fileServer);
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}