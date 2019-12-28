package common;

/**
 * Defines constants used by both client and server.
 */
public class Constants {
    /**
     * Separates a message type specifier from the message body.
     */
    public static final String MSG_TYPE_DELIMETER = "##";
    /**
     * Separates the username from the password
     */
    public static final String LOGIN_DELIMITER = "@@";
    /**
     * The message type specifier is the first token in a message.
     */
    public static final int MSG_TYPE_INDEX = 0;
    /**
     * The login information specifier is the second token in a message.
     */
    public static final int MSG_JWT_LOGIN_INDEX = 1;
    /**
     * The message body is the third token in a message.
     */
    public static final int MSG_BODY_INDEX = 2;

    /**
     * The max length of a game entry.
     */
    public static final int MAX_MSG_LENGTH = 8192;

    /**
     * Separates a message length header.
     */
    public static final String MSG_LEN_DELIMETER = "###";

}
