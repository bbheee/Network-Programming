package server.controller;

import server.model.GameSession;

import java.util.ArrayList;

/**
 * The server side controller. All calls to the server side model pass through here.
 */
public class Controller {

    private GameSession game;

    public Controller(ArrayList<String> wordlist) {
        game = new GameSession(wordlist);
    }

    public void startGame() {
        game.newGame();
    }

    public void guess(String word) {
        game.guess(word);
    }

    public String[] getGameState() {
        return game.getState();
    }

}
