package common;

/**
 * Defines all messages that can be sent between client and server
 */
public enum MsgType {
    /**
     * The user starts the game.
     */
    START_GAME,

    /**
     * The user makes a guess for a letter or a word.
     */
    GUESS,

    /**
     * Client is about to close, all server resources related to the sending client should be
     * released.
     */
    DISCONNECT,

    /**
     * Sent from the Server to the client
     */
    STATE,

    /**
     * Login with username and password
     */
    LOGIN
}
