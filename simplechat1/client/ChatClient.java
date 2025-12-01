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
public class ChatClient extends ObservableClient
{
  //Instance variables **********************************************

  private boolean manualDisconnect = false;
  private String loginId;

  
  //Constructors ****************************************************

  /**
   * Constructs an instance of the chat client.
   *
   * @param loginId The user's login ID.
   * @param host The server to connect to.
   * @param port The port number to connect on.
   */

  public ChatClient(String loginId, String host, int port)
    throws IOException
  {
    super(host, port); //Call the superclass constructor
    this.loginId = loginId;
    openConnection();
  }

  
  //Instance methods ************************************************
    
  /**
   * Called automatically when the connection to the server is closed.
   * Notifies observers about the connection closure.
   */
  @Override
  protected void connectionClosed() {
      super.connectionClosed(); // Notifies observers with CONNECTION_CLOSED

      boolean oldStatus = manualDisconnect;
      manualDisconnect = false;

      if (!oldStatus) {
        setChanged();
        notifyObservers("Connection closed. Exiting client.");
      }
  }

  /**
   * Called automatically when a connection exception occurs.
   * Notifies observers about the connection exception.
   *
   * @param exception The exception thrown by the connection.
   */
  @Override
  protected void connectionException(Exception exception) {
      super.connectionException(exception); // Notifies observers with CONNECTION_EXCEPTION

      boolean oldStatus = manualDisconnect;
      manualDisconnect = false;

      if (!oldStatus) {
        setChanged();
        notifyObservers("Connection error: " + exception.getMessage() + "; Exiting client.");
      }
  }

  /**
   * Called automatically when a connection is established.
   * Sends the login command and notifies observers.
   */
  @Override
  protected void connectionEstablished() {
      super.connectionEstablished(); // Notifies observers with CONNECTION_ESTABLISHED

      try {
          sendToServer("#login " + loginId);
          setChanged();
          notifyObservers("Sent login command to server: #login " + loginId);
      } catch (IOException e) {
          setChanged();
          notifyObservers("Error sending login command: " + e.getMessage());
      }
  }


  /**
   * This method handles all data that comes in from the server.
   * The super method already notifies observers with the message.
   *
   * @param msg The message from the server.
   */
  @Override
  protected void handleMessageFromServer(Object msg)
  {
    super.handleMessageFromServer(msg); // Notifies observers with msg
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
                      setChanged();
                      notifyObservers("Error closing connection: " + e.getMessage());
                  }
                  setChanged();
                  notifyObservers("Logged off.");
          } else {
              setChanged();
              notifyObservers("Already logged off.");
          }
        break;
        case "#sethost":
            if (isConnected()) {
              setChanged();
              notifyObservers("Error: must log off first.");
            } else if (parts.length < 2 || parts[1].isBlank()) {
              setChanged();
              notifyObservers("Usage: #sethost <host>");
            } else {
              setHost(parts[1].trim());
              setChanged();
              notifyObservers("Host set to: " + getHost());
            }
        break;
        case "#setport":
              if (isConnected()) {
                setChanged();
                notifyObservers("Error: must log off first.");
              } else if (parts.length < 2) {
                setChanged();
                notifyObservers("Usage: #setport <port>");
              } else {
                try {
                  int p = Integer.parseInt(parts[1].trim());
                  setPort(p);
                  setChanged();
                  notifyObservers("Port set to: " + getPort());
                } catch (NumberFormatException nfe) {
                   setChanged();
                   notifyObservers("Port must be a number.");
                }
              }
          break;
        case "#login":
          if (!isConnected()) {
            try {
              openConnection();
              setChanged();
              notifyObservers("Logged in to " + getHost() + ":" + getPort());
            } catch (IOException e) {
              setChanged();
              notifyObservers("Error opening connection: " + e.getMessage());
            }
          } else {
            setChanged();
            notifyObservers("Already connected.");
          }
          break;
          case "#gethost":
              setChanged();
              notifyObservers("Host: " + getHost());
              break;
          case "#getport":
              setChanged();
              notifyObservers("Port: " + getPort());
              break;
          default:
              setChanged();
              notifyObservers("Unknown command.");
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
      setChanged();
      notifyObservers("Could not send message to server. Terminating client.");
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
