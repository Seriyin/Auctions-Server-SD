
package auctionsserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/** 
 * AuctionReaders are in charge of handling all input from user
 * and delegating that input to its ClientsManager.
 * AuctionReaders instantiate their own writers.
 * Management of communication is delegated to its ClientsManager.
 *
 * @author Andre
 *
 */
public class AuctionsServerWorkerReader implements Runnable 
{
    private final Socket sckt;
    private final ClientsManager clm;
    private BufferedReader bf;
    private String user;
    private String password;
    
    /**
     * 
     * @param sckt The client socket this worker handles
     * @param clm The ClientsManager this worker delegates communication
     * and sanitized input to.
     */
    public AuctionsServerWorkerReader(Socket sckt, ClientsManager clm) 
    {
        this.sckt=sckt;
        this.clm=clm;
    }

    private BufferedReader initReaderToSocket(Socket sckt) 
    {
        InputStream strm;
        try 
        {
            strm = sckt.getInputStream();            
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
        if ((bf=initReaderToSocket(sckt))==null)
        {
            return;
        }
        boolean successfulLogin;
        successfulLogin=attemptLogin();
        if (successfulLogin) 
        {
            createWorkerWriter();
            String str;
            try
            {
                while ((str=bf.readLine())!=null) 
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
            sckt.close();
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        try 
        {
            ThreadQueueSingleton.Instance().put(Thread.currentThread());
        } 
        catch (InterruptedException ex) 
        {
            ex.printStackTrace();
        }
    }

    
    private boolean attemptLogin() 
    {
        boolean successfulLogin=false;
        try 
        { 
            do 
            {
                boolean isLogin = bf.readLine().equals("0");
                user=bf.readLine();
                password = null;
                if (user!=null)
                    password=bf.readLine();
                if (password!=null) 
                { 
                    if (isLogin)
                    {
                        successfulLogin=clm.loginUser(user,password);
                    }
                    else
                    {
                        successfulLogin=clm.registerUser(user,password);
                    }
                }
            }
            while (successfulLogin);
        }
        catch(IOException ex) {}
        return successfulLogin;
    }

    private void handleInput(String str) {
        String[] inputSplit = str.trim().split("|");
        if (inputSplit.length>=1) 
        {
            if (inputSplit[0].equals("C")) 
            {
                if(inputSplit.length==1) 
                {
                    try
                    {
                        clm.listClients(inputSplit[1]);
                    }
                    catch(NumberFormatException e){}                
                } 
                else if(inputSplit.length==2)
                {
                    try
                    {
                        long hash=Long.parseLong(inputSplit[1]);
                        float value=Float.parseFloat(inputSplit[2]);
                        clm.registerBid(hash,value,user);
                    }
                    catch(NumberFormatException e){}
                }
            }
            else if (inputSplit[0].equals("V") && inputSplit.length==2) 
            {
                try
                {
                    clm.registerAuction(user,inputSplit[1]);
                }
                catch(NumberFormatException e) {}                
            }
        }
    }

    private void createWorkerWriter() 
    {
        Thread t= new Thread(new AuctionsServerWorkerWriter(user,clm,sckt));
        t.start();
    }
    
}
