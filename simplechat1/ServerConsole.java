import java.io.*;
import ocsf.server.*;
import common.*;

/**
 * ServerConsole allows server admin to type messages/commands.
 */
public class ServerConsole implements ChatIF {
    //Class variables *************************************************
  
    /**
    * The default port to connect on.
    */
    final public static int DEFAULT_PORT = 5555;

    //Instance variables **********************************************
  
    /**
   * The instance of the client that created this ConsoleChat.
   */
    public EchoServer server;

    //Constructors ****************************************************

    /**
     * Constructs an instance of the ServerConsole UI.
     *
     * @param port The port to start on.
     */
    public ServerConsole(int port) {
        server = new EchoServer(port, this);
    }

    //Instance methods ************************************************
  
    /**
    * This method waits for input from the console.  Once it is 
    * received, it sends it to the client's message handler.
    */

   /**
     * Displays a message to the ServerConsole UI.
     *
     * @param message The message to print.
    */
    public void display(String message) {
        System.out.println("> " + message);
    }

  /**
   * Parses and executes server-side commands starting with '#'
   * 
   * @param cmdLine the data passed in the UI.
   */
    private void handleCommand(String cmdLine) {
        String[] parts = cmdLine.split("\\s+");

        String cmd = parts[0].toLowerCase();
        switch (cmd) {
            case "#quit":
                try { server.close(); } catch (IOException ignored) {}
                System.exit(0);
                break;
            case "#stop":
                try { server.stopListening(); } catch (Exception e) { display("Already stopped."); }
                break;
            case "#close":
                try { server.close(); } catch (Exception e) { display("Error closing."); }
                break;
            case "#setport":
            if (server.isListening()) {
                display("Cannot change port while server is open. Use #close first.");
            } else if (parts.length < 2) {
                display("Usage: #setport <port>");
            } else {
                try {
                    int p = Integer.parseInt(parts[1].trim());
                    server.setPort(p);
                    display("Port set to " + server.getPort());
                } catch (NumberFormatException nfe) {
                    display("Port must be a number.");
                }
            }
            break;
            case "#start":
                if (!server.isListening())
                    try { server.listen(); } catch (Exception e) { display("Start failed."); }
                else display("Server already listening.");
                break;
            case "#getport":
                display("Port: " + server.getPort());
                break;
            default:
                display("Unknown command.");
        }
    }

  /**
   * This method waits for input from the console.  Once it is 
   * received, it handles it.
   */
    public void accept() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String msg;
            while (true) {
                msg = br.readLine();
                if (msg.startsWith("#"))
                    handleCommand(msg);
                else {
                    display("SERVER msg> " + msg);
                    server.sendToAllClients("SERVER msg> " + msg);
                }
            }
        } catch (IOException e) {
            display("Unexpected I/O error");
        }
    }
}