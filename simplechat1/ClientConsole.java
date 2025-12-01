// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import client.*;
import common.*;
import java.util.Observer;
import java.util.Observable;
import ocsf.client.ObservableClient;

/**
 * This class constructs the UI for a chat client.  It implements the
 * chat interface in order to activate the display() method.
 * Warning: Some of the code here is cloned in ServerConsole
 *
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @version July 2000
 */
public class ClientConsole implements ChatIF, Observer 
{
  //Class variables *************************************************
  
  /**
   * The default port to connect on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  //Instance variables **********************************************
  
  /**
   * The instance of the client that created this ConsoleChat.
   */
  ChatClient client;

  
  //Constructors ****************************************************

  /**
   * Constructs an instance of the ClientConsole UI.
   *
   * @param loginId The user login name.
   * @param host The host to connect to.
   * @param port The port to connect on.
   */
  public ClientConsole(String loginId, String host, int port)
  {
    try
    {
      client = new ChatClient(loginId, host, port);
      client.addObserver(this); // Register as observer
    }
    catch(IOException exception)
    {
      System.out.println("Error: Can't setup connection!"
                + " Terminating client.");
      System.exit(1);
    }
  }

  
  //Instance methods ************************************************

  /**
   * This method is called when the observed object (ChatClient) changes.
   * It handles all notifications from the client.
   *
   * @param obs The observable object (the ChatClient)
   * @param arg The notification argument (message or event string)
   */
  @Override
  public void update(Observable obs, Object arg) {
      // Handle different types of notifications
      if (arg instanceof String) {
          String message = (String) arg;

          // Check for special event notifications
          if (message.equals(ObservableClient.CONNECTION_CLOSED)) {
              display("Connection closed. Exiting client.");
              System.exit(0);
          }
          else if (message.equals(ObservableClient.CONNECTION_EXCEPTION)) {
              display("Connection error. Exiting client.");
              System.exit(0);
          }
          else if (message.equals(ObservableClient.CONNECTION_ESTABLISHED)) {
              display("Connection established.");
          }
          else {
              // Regular message
              display(message);
          }
      } else {
          // Handle any other object type
          display(arg.toString());
      }
  }

  /**
   * This method waits for input from the console.  Once it is
   * received, it sends it to the client's message handler.
   */
  public void accept() 
  {
    try
    {
      BufferedReader fromConsole = 
        new BufferedReader(new InputStreamReader(System.in));
      String message;

      while (true) 
      {
        message = fromConsole.readLine();
        client.handleMessageFromClientUI(message);
      }
    } 
    catch (Exception ex) 
    {
      System.out.println
        ("Unexpected error while reading from console!");
    }
  }

  /**
   * This method overrides the method in the ChatIF interface.  It
   * displays a message onto the screen.
   *
   * @param message The string to be displayed.
   */
  public void display(String message) 
  {
    System.out.println("> " + message);
  }

  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of the Client UI.
   *
   * @param args[0] The user's login name.
   * @param args[1] The host to connect to.
   * @param args[2] The port to connect to.
   */
  public static void main(String[] args)
  {
      String loginId;
      String host = "localhost";
      int port = DEFAULT_PORT;

      if (args.length < 1) {
          System.out.println("Error: Did not input login id!");
          System.exit(1);
      }

      loginId = args[0];

      if (args.length > 1) {
          host = args[1];
      }

      if (args.length > 2) {
          try {
              port = Integer.parseInt(args[2]);
          } catch (NumberFormatException e) {
              System.out.println("Invalid port number!!!!! Using default " + DEFAULT_PORT);
          }
      }

      System.out.println("About to connect");

      ClientConsole chat = new ClientConsole(loginId, host, port);
      chat.accept();
  }
}

//End of ConsoleChat class
