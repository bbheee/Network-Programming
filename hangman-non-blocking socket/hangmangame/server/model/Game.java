package server.model;

import java.util.Arrays;

public class Game {

    private String word;
    private int chances;
    private char[] guesses;


    public Game(String word) {
        this.word = word;
        this.chances = word.length();
        this.guesses = new char[word.length()];
        Arrays.fill(guesses, '_');
    }

    public boolean guess(String guess) {
        if (guess.length() != 1 && guess.length() != word.length()) {
            chances--;
            return false;
        }
        if (chances > 0) {
            if (word.contains(guess)) {
                if (word.equals(guess)) {
                    chances = 0;
                    guesses = word.toCharArray();
                } else if (Arrays.toString(guesses).contains(guess)) {
                } else {
                    char guessChar = guess.charAt(0);
                    for (int i = 0; i < word.length(); i++) {
                        if (guessChar == word.charAt(i)) {
                            guesses[i] = guessChar;
                        }
                    }
                }
            } else {
                chances--;
            }
        } else {
            return false;
        }
        return true;
    }

    public String[] getState() {
        return new String[]{new String(guesses), String.valueOf(chances)};
    }

}
