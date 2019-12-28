package client.controller;

import client.net.OutputHandler;
import client.net.ServerConnection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

/**
 * This controller decouples the view from the network layer. All methods, except
 * <code>disconnect</code>, submit their task to the common thread pool, provided by
 * <code>ForkJoinPool.commonPool</code>, and then return immediately.
 */
public class Controller {
    private final ServerConnection serverConnection = new ServerConnection();

    /**
     * @see ServerConnection#connect(java.lang.String, int,
     * OutputHandler)
     */
    public void connect(String host, OutputHandler outputHandler) {
        CompletableFuture.runAsync(() -> {
            try {
                serverConnection.connect(host, 8080, outputHandler);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }).thenRun(() -> outputHandler.handleMsg("Connected to " + host + ":" + 8080));
    }

    /**
     * @see ServerConnection#disconnect() Blocks until disconnection is completed.
     */
    public void disconnect() throws IOException {
        serverConnection.disconnect();
    }

    /**
     * @see ServerConnection#sendGuessEntry(String) (java.lang.String)
     */
    public void sendGuess(String token, String guess) {
        CompletableFuture.runAsync(() -> serverConnection.sendGuessEntry(token, guess));
    }

    /**
     * @see ServerConnection#sendStartGameEntry() (java.lang.String)
     */
    public void sendStartGame(String token) {
        CompletableFuture.runAsync(() -> serverConnection.sendStartGameEntry(token));
    }

    public void userLogin(String username, String password) {
        CompletableFuture.runAsync(() -> serverConnection.sendLogin(username, password));
    }
}
