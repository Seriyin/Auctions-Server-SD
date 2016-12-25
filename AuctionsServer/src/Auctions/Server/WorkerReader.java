
package Auctions.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

/** 
 * AuctionReaders are in charge of handling all input from clients
 * and delegating that input to its ClientsManager or AuctionsManager,
 * according to if said input is clients-related or auctions-related.
 * Management of communication is delegated to its Managers as well.
 *
 * @author Andre
 *
 */
public class WorkerReader implements Runnable 
{
    private final Socket SocketToRead;
    private final ClientsManager ClientsManager;
    private final AuctionsManager AuctionsManager;
    private final BufferedReader SocketInput;
    private String User;
    private String Password;
    
    /**
     * 
     * @param SocketToRead The client socket this worker handles
     * @param ClientsManager The ClientsManager this worker delegates 
     * client-specific input to.
     * @param AuctionsManager The AuctionsManager this worker delegates
     * auctions-specific input to.
     * @param SocketInput A reader over a socket input stream.
     */
    public WorkerReader(Socket SocketToRead, 
                        ClientsManager ClientsManager,
                        AuctionsManager AuctionsManager,
                        BufferedReader SocketInput) 
    {
        this.SocketToRead=SocketToRead;
        this.ClientsManager=ClientsManager;
        this.AuctionsManager=AuctionsManager;
        this.SocketInput=SocketInput;
    }

    
    @Override
    public void run() 
    {
        System.out.println("Worker Reader Start");
        boolean successfulLogin;
        successfulLogin=attemptLogin();
        if (successfulLogin) 
        {
            attemptReads();
        }  
        try
        {
            SocketToRead.close();
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }


    
    /**
     * A login is indicate by a 0.
     * A registration is indicated by not a 0. Implementation dependent.
     * A cycle of attempting to login. Fails if a socket dies before
     * a successful authentication.
     * A registration does not login a user.
     * @return if the login was successful.
     */
    private boolean attemptLogin() 
    {
        boolean SuccessfulLogin=false;
        try 
        { 
            while (!SuccessfulLogin){
                boolean IsLogin = SocketInput.readLine().equals("0");
                User=SocketInput.readLine();
                Password = null;
                if (User!=null)
                    Password=SocketInput.readLine();
                if (Password!=null) 
                {                    
                    if (IsLogin)
                    {
                        SuccessfulLogin=
                                ClientsManager.loginUser(User,
                                                         Password,
                                                         SocketToRead);
                    }
                    else
                    {
                        ClientsManager.registerUser(User,
                                                    Password,
                                                    SocketToRead);
                    }
                }
            }            
        }
        catch(IOException ex) {}
        return SuccessfulLogin;
    }

    /**
     * A cycle of attempting to read from a socket line-by-line.
     * Fails when a socket dies.
     * Delegates handling and parsing that input to the AuctionsManager,
     * as a Reader does not have access to a TaskPool.
     * Reading and parsing can be independent.
     */
    private void attemptReads() {
        String ToParse;
        try
        {
            while ((ToParse=SocketInput.readLine())!=null) 
            {
                AuctionsManager.handleAuctionInput(User,
                                                   ToParse,
                                                   ClientsManager);
            }
        }
        catch (IOException e) {}
    }

    

    
}
