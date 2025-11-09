// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import ocsf.server.*;
import common.ChatIF;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;

  // Server UI
  private ChatIF serverUI;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of EchoServer with an UI.
   *
   * @param port the port number to listen on
   * @param serverUI the user interface (for server messages)
   */
  public EchoServer(int port, ChatIF serverUI) {
      super(port);
      this.serverUI = serverUI;
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  @Override
  public void handleMessageFromClient(Object msg, ConnectionToClient client) {
      String message = msg.toString().trim();
      String loginId = (String) client.getInfo("loginId");

      // 1. Check if this is a #login command
      if (message.startsWith("#login")) {
          if (loginId != null) { // if null, we can say already logged on the in
              try {
                  client.sendToClient("Error: Already logged in as " + loginId);
              } catch (IOException e) { /* what to do here? */ }
              return;
          }

          // 1.1 read login id
          String[] parts = message.split("\\s+", 2);
          if (parts.length < 2 || parts[1].trim().isEmpty()) {
              try {
                  client.sendToClient("Error: CMD Usage is #login <loginId>");
              } catch (IOException e) { /* what do to here? */ }
              return;
          }

          // 1.2 Save loginId
          loginId = parts[1].trim();
          client.setInfo("loginId", loginId);

          serverUI.display("Client logged in with ID: " + loginId);
          try {
              client.sendToClient("Login working! Welcome " + loginId + "!");
          } catch (IOException e) { /* what to do here? */ }

          return;
      }

      // 2. For any other message, check if the client has logged in
      if (loginId == null) {
          try {
              client.sendToClient("Error: Must login with first command which is #login <loginId>!!!");
              client.close();
          } catch (IOException e) {
              serverUI.display("Error closing client connection: " + e.getMessage());
          }
          return;
      }

      // 3. Prefix message with login id and broadcast
      serverUI.display("Message received from " + loginId + ": " + message);
      this.sendToAllClients(loginId + "> " + message);
  }

  /**
   * This method is invoked when a new client connects.
   *
   * @param client The connection from which the message originated.
   */
  @Override
  protected void clientConnected(ConnectionToClient client) {
      serverUI.display("Client connected: " + client);
  }

  /**
   * This method is invoked when a client disconnects.
   *
   * @param client The connection from which the message originated.
   */
  @Override
  protected void clientDisconnected(ConnectionToClient client) {
      serverUI.display("Client disconnected: " + client);
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    serverUI.display("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    serverUI.display("Server has stopped listening for connections.");
  }
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
    public static void main(String[] args) {
        int port; // Port to listen on

        try {
            port = Integer.parseInt(args[0]);
        } catch (Throwable t) {
            port = DEFAULT_PORT; // Default 5555
        }

        ServerConsole console = new ServerConsole(port);

        try {
            console.server.listen(); // start listening
        } catch (Exception ex) {
            console.display("ERROR - Could not listen for clients!");
        }

        // Start reading from console input
        console.accept();
    }
}
//End of EchoServer class
