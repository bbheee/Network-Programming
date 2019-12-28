package client.view;

import client.controller.Controller;
import client.net.OutputHandler;
import common.Constants;
import common.MsgType;

import java.util.Scanner;

/**
 * Reads and interprets user commands. The command interpreter will run in a separate thread, which
 * is started by calling the <code>start</code> method. Commands are executed in a thread pool, a
 * new prompt will be displayed as soon as a command is submitted to the pool, without waiting for
 * command execution to complete.
 */
public class CommandInterpreter implements Runnable {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCmds = false;
    private Controller contr;
    private final ThreadSafeStdOut outMgr = new ThreadSafeStdOut();
    private String token = null;

    /**
     * Starts the interpreter. The interpreter will be waiting for user input when this method
     * returns. Calling <code>start</code> on an interpreter that is already started has no effect.
     */
    public void start() {
        if (receivingCmds) {
            return;
        }
        receivingCmds = true;
        contr = new Controller();
        new Thread(this).start();
    }

    /**
     * Interprets and performs user commands.
     */
    @Override
    public void run() {
        try {
            contr.connect("localhost",
                    new ConsoleOutput());
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        while (receivingCmds) {
            try {
                String input = readNextLine();
                String[] command = getCommand(input);
                Command cmd = Command.valueOf(command[0].toUpperCase());

                switch (cmd) {
                    case LOGIN:
                        contr.userLogin(command[1], command[2]);
                        break;
                    case START_GAME:
                        contr.sendStartGame(token);
                        break;
                    case GUESS:
                        contr.sendGuess(token, command[1]);
                        break;
                    default:
                        continue;
                }
            } catch (Exception e) {
                outMgr.println("Operation failed");
            }
        }
    }

    private String readNextLine() {
        outMgr.print(PROMPT);
        return console.nextLine();
    }

    public class ConsoleOutput implements OutputHandler {
        @Override
        public void handleMsg(String msg) {
            if (!msg.contains(Constants.MSG_TYPE_DELIMETER)) {
                outMgr.println(msg);
                outMgr.print(PROMPT);
                return;
            }

            String[] splitMessage = msg.split(Constants.MSG_TYPE_DELIMETER);
            try {

                String head = splitMessage[0];
                String body = splitMessage[1];

                MsgType msgType = MsgType.valueOf(head.toUpperCase());
                switch (msgType) {
                    case STATE:
                        String[] bod = body.split(":");
                        outMgr.println("Current word looks like this: " + bod[0]);
                        outMgr.println("Chances left: " + bod[1]);
                        outMgr.println("Current score: " + bod[2]);
                        outMgr.print(PROMPT);
                        break;
                    case LOGIN:
                        outMgr.println("Login successful");
                        outMgr.print(PROMPT);
                        token = body;
                        break;
                    default:
                        outMgr.err("Not a valid header");
                        outMgr.print(PROMPT);
                }
            } catch (Exception e) {
                outMgr.err(e.getMessage());
                outMgr.print(PROMPT);
            }

        }
    }

    private String[] getCommand(String input) {
        String[] cmd = input.split(" ");
        return cmd;
    }
}
