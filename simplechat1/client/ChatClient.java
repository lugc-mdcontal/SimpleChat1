// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import common.*;
import java.io.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 
  private boolean manualDisconnect = false;
  private String loginId;

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String loginId, String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    this.loginId = loginId;
    openConnection();
  }

  
  //Instance methods ************************************************
    
  /**
   * Called automatically when the connection to the server is closed.
   * Displays a shutdown message and exits the program.
   */
  @Override
  protected void connectionClosed() {
      boolean oldStatus = manualDisconnect;
      manualDisconnect = false;

      if (!oldStatus) { // the pdf you provided (dr. wei) requires this because you say logoff should disconnect and not quit, but this handler is overwritten and you told us to quit on this handler.
        clientUI.display("Connection closed. Exiting client.");
        System.exit(0); // connection is already closed
      }
  }

  /**
   * Called automatically when a connection exception occurs.
   * Displays the exception message and exits the program.
   *
   * @param exception The exception thrown by the connection.
   */
  @Override
  protected void connectionException(Exception exception) {
      boolean oldStatus = manualDisconnect;
      manualDisconnect = false;

      if (!oldStatus) {
        clientUI.display("Connection error: " + exception.getMessage() + "; Exiting client.");
        System.exit(0); // connection is already closed
      }
  }

  @Override
  protected void connectionEstablished() {
      try {
          sendToServer("#login " + loginId);
          clientUI.display("Sent login command on the directly server: #login " + loginId);
      } catch (IOException e) {
          clientUI.display("Error sending login command?: " + e.getMessage());
      }
  }


  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
    clientUI.display(msg.toString());
  }

  /**
   * Parses and executes client-side commands starting with '#'
   * 
   * @param cmdLine the data passed in the UI.
   */
  private void handleCommand(String cmdLine) {
      String[] parts = cmdLine.split("\\s+", 2);

      String cmd = parts[0].toLowerCase();
      switch (cmd) {
          case "#quit":
              quit();
          break;
          case "#logoff":
              if (isConnected()) {
                  try {
                      manualDisconnect = true;
                      closeConnection();
                  } catch (IOException e) {
                      clientUI.display("Error closing connection: " + e.getMessage());
                  }
                  clientUI.display("Logged off.");
          } else clientUI.display("Already logged off.");
        break;
        case "#sethost":
            if (isConnected()) {
              clientUI.display("Error: must log off first.");
            } else if (parts.length < 2 || parts[1].isBlank()) {
              clientUI.display("Usage: #sethost <host>");
            } else {
              setHost(parts[1].trim());
              clientUI.display("Host set to: " + getHost());
            }
        break;
        case "#setport":
              if (isConnected()) {
                clientUI.display("Error: must log off first.");
              } else if (parts.length < 2) {
                clientUI.display("Usage: #setport <port>");
              } else {
                try {
                  int p = Integer.parseInt(parts[1].trim());
                  setPort(p);
                  clientUI.display("Port set to: " + getPort());
                } catch (NumberFormatException nfe) {
                   clientUI.display("Port must be a number.");
                }
              }
          break;
        case "#login":
          if (!isConnected()) {
            try {
              openConnection();
              clientUI.display("Logged in to " + getHost() + ":" + getPort());
            } catch (IOException e) {
              clientUI.display("Error opening connection: " + e.getMessage());
            }
          } else {
            clientUI.display("Already connected.");
          }
          break;
          case "#gethost":
              clientUI.display("Host: " + getHost());
              break;
          case "#getport":
              clientUI.display("Port: " + getPort());
              break;
          default:
              clientUI.display("Unknown command.");
      }
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message, or command, from the UI.    
   */
  public void handleMessageFromClientUI(String message)
  {
    try
    {
      // Handle command.
      if (message.startsWith("#")) {
          handleCommand(message);
          return;
      }

      sendToServer(message);
    }
    catch(Exception e)
    {
      clientUI.display
        ("Could not send message to server.  Terminating client.");
      quit();
    }
  }
  
  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
}
//End of ChatClient class
