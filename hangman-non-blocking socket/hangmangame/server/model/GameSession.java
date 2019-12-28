package server.model;

import java.util.ArrayList;
import java.util.Random;

/**
 * Game session
 */
public class GameSession {
    private ArrayList<String> wordList;
    private Game game;
    private String[] state = new String[]{"", "", ""};
    private int score = 0;
    private Random randomGenerator = new Random();


    public GameSession(ArrayList<String> wordList) {
        this.wordList = wordList;
    }

    public void newGame() {
        int wordSeed = randomGenerator.nextInt(wordList.size());
        game = new Game(wordList.get(wordSeed));
        state = null;
    }

    public boolean guess(String guess) {
        try {
            return game.guess(guess);
        } catch (Exception e) {
            return false;
        }
    }

    public String[] getState() {

        if (this.state != null) {
            return this.state;
        }

        String[] state = game.getState();
        if (state[1].equals("0")) {
            if (state[0].contains("_")) {
                score--;
            } else {
                score++;
                state[1] = "";
            }
            this.state = new String[]{state[0], state[1], String.valueOf(score)};
        }
        return new String[]{state[0], state[1], String.valueOf(score)}; // {word, chances, score}

    }

}
