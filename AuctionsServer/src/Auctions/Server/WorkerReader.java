
package Auctions.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/** 
 * AuctionReaders are in charge of handling all input from clients
 * and delegating that input to its ClientsManager.
 * AuctionReaders delegate writers instantiation to ClientsManager
 * Management of communication is delegated to its ClientsManager as well.
 *
 * @author Andre
 *
 */
public class WorkerReader implements Runnable 
{
    private final Socket SocketToRead;
    private final ClientsManager WorkerClientsManager;
    private BufferedReader SocketInput;
    private String User;
    private String Password;
    
    /**
     * 
     * @param SocketToRead The client socket this worker handles
     * @param WorkerClientsManager The ClientsManager this worker delegates 
     * communication and sanitized input to.
     */
    public WorkerReader(Socket SocketToRead, 
                        ClientsManager WorkerClientsManager) 
    {
        this.SocketToRead=SocketToRead;
        this.WorkerClientsManager=WorkerClientsManager;
    }

    private BufferedReader initReaderToSocket(Socket SocketToRead) 
    {
        InputStream strm;
        try 
        {
            strm = SocketToRead.getInputStream();            
        }
        catch(IOException e) 
        {
            return null;
        }
        return new BufferedReader
                        (new InputStreamReader(strm));
    }
    
    @Override
    public void run() 
    {
        System.out.println("Worker Reader Start");
        if ((SocketInput=initReaderToSocket(SocketToRead))==null)
        {
            return;
        }
        boolean successfulLogin;
        successfulLogin=attemptLogin();
        if (successfulLogin) 
        {
            String str;
            try
            {
                while ((str=SocketInput.readLine())!=null) 
                {
                    handleInput(str);
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
            do 
            {
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
                            WorkerClientsManager.loginUser(User,
                                                           Password,
                                                           SocketToRead);
                    }
                    else
                    {
                        SuccessfulLogin=
                            WorkerClientsManager.registerUser(User,
                                                              Password,
                                                              SocketToRead);
                    }
                }
            }
            while (SuccessfulLogin);
        }
        catch(IOException ex) {}
        return SuccessfulLogin;
    }

    
    private void handleInput(String str) {
        String[] InputSplit = str.trim().split("|");
        if (InputSplit.length>=1) 
        {
            //There is a command by a client 'C'
            if (InputSplit[0].equals("C")) 
            {
                //There is just the command, it's a request to read.
                if(InputSplit.length==1) 
                {
                    try
                    {
                        WorkerClientsManager.listClients(User);
                    }
                    catch(NumberFormatException e){}                
                } 
                //There is a command and two arguments, it's a bid.
                else if(InputSplit.length==3)
                {
                    try
                    {
                        long BidHash=Long.parseLong(InputSplit[1]);
                        float ValueToBid=Float.parseFloat(InputSplit[2]);
                        WorkerClientsManager.registerBid(BidHash,ValueToBid,User);
                    }
                    catch(NumberFormatException e){}
                }
            }
            //There is a command by a vendor.
            else if (InputSplit[0].equals("V")) 
            { 
                //If there is an argument try parsing an auction code
                //Otherwise it's an auction registration
                if(InputSplit.length==2) 
                {
                    try
                    {
                        long AuctionCode=Long.parseLong(InputSplit[1]);
                        WorkerClientsManager.endAuction(User,AuctionCode);
                    }
                    catch(NumberFormatException e) 
                    {
                        WorkerClientsManager.registerAuction(User,InputSplit[1]);                        
                    }
                }                
            }
        }
    }

    
}
