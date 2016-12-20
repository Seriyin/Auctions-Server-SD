
package auctionsserver;

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
     * @param sckt The client socket this worker handles
     * @param clm The ClientsManager this worker delegates communication
     * and sanitized input to.
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

    
    private boolean attemptLogin() 
    {
        boolean successfulLogin=false;
        try 
        { 
            do 
            {
                boolean isLogin = SocketInput.readLine().equals("0");
                User=SocketInput.readLine();
                Password = null;
                if (User!=null)
                    Password=SocketInput.readLine();
                if (Password!=null) 
                { 
                    if (isLogin)
                    {
                        successfulLogin=
                                WorkerClientsManager.loginUser(User,Password);
                    }
                    else
                    {
                        successfulLogin=
                            WorkerClientsManager.registerUser(User,Password);
                    }
                }
            }
            while (successfulLogin);
        }
        catch(IOException ex) {}
        return successfulLogin;
    }

    private void handleInput(String str) {
        String[] InputSplit = str.trim().split("|");
        if (InputSplit.length>=1) 
        {
            if (InputSplit[0].equals("C")) 
            {
                if(InputSplit.length==1) 
                {
                    try
                    {
                        WorkerClientsManager.listClients(InputSplit[1]);
                    }
                    catch(NumberFormatException e){}                
                } 
                else if(InputSplit.length==2)
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
            else if (InputSplit[0].equals("V") && InputSplit.length==2) 
            {
                try
                {
                    WorkerClientsManager.registerAuction(User,InputSplit[1]);
                }
                catch(NumberFormatException e) {}                
            }
        }
    }

    
}
