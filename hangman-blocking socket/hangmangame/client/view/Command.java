package client.view;

/**
 * Defines all commands that can be performed by a user of the Hangman GameSession
 */
public enum Command {
    /**
     * Starts a new game
     */
    START_GAME,

    /**
     * Login with username and password
     */

    LOGIN,

    /**
     * Specifies a guess for either a letter or word for the hangman game
     */
    GUESS,

    /**
     * No command
     */

    NO_COMMAND

}
