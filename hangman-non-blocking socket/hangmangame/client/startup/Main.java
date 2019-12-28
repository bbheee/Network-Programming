package client.startup;

import client.view.CommandInterpreter;

/**
 * Starts the hangman game client.
 */
public class Main {
    /**
     * @param args There are no command line arguments.
     */

    public static void main(String[] args) {
        new CommandInterpreter().start();
    }
}
