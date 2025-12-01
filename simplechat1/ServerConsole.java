import java.io.*;
import ocsf.server.*;
import common.*;
import java.util.Observer;
import java.util.Observable;

/**
 * ServerConsole allows server admin to type messages/commands.
 */
public class ServerConsole implements ChatIF, Observer {
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
        server = new EchoServer(port);
        server.addObserver(this); // Register as observer
    }

    //Instance methods ************************************************

    /**
     * This method is called when the observed object (EchoServer) changes.
     * It handles all notifications from the server.
     *
     * @param obs The observable object (the EchoServer)
     * @param arg The notification argument (OriginatorMessage or String)
     */
    @Override
    public void update(Observable obs, Object arg) {
        // Handle OriginatorMessage on the here
        if (arg instanceof OriginatorMessage) {
            OriginatorMessage om = (OriginatorMessage) arg;
            Object message = om.getMessage();

            // Check for server event constants
            if (message.equals(ObservableOriginatorServer.SERVER_STARTED)) {
                // Alrady handled
            }
            else if (message.equals(ObservableOriginatorServer.SERVER_STOPPED)) {
                // Already handled
            }
            else if (message.equals(ObservableOriginatorServer.SERVER_CLOSED)) {
                display("Server closed.");
            }
            else if (message.equals(ObservableOriginatorServer.CLIENT_CONNECTED)) {
                // Already handled 
            }
            else if (message.equals(ObservableOriginatorServer.CLIENT_DISCONNECTED)) {
                // Already handled
            }
            else if (message.equals(ObservableOriginatorServer.CLIENT_EXCEPTION)) {
                display("Client exception occurred.");
            }
            else if (message.equals(ObservableOriginatorServer.LISTENING_EXCEPTION)) {
                display("Listening exception occurred.");
            }
            else {
                // EchoServer already broadcasts it
            }
        }
        else if (arg instanceof String) {
            // Custom string notifications from EchoServer
            display((String) arg);
        }
        else {
            // Fallback for any other object type
            display(arg.toString());
        }
    }

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
                if (msg == null) {
                    break; // End of input stream
                }
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