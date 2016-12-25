
package Auctions.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
     * communication and sanitized input to.
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
            catch (IOException e) 
            {
                e.printStackTrace();
            }
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


    
    
    /** A login is indicate by a 0.
     * A registration is indicated by not a 0. Implementation dependent.
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
                        SuccessfulLogin=
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

    

    
}
