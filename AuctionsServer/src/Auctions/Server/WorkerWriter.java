/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Workers that write Strings from a client's log to the corresponding socket.
 * A client's log contains only important information that must remain even
 * when offline.
 * Workers begin by writing from TempLogs on a corresponding socket before
 * authentication.
 * @author André Diogo, Gonçalo Pereira, António Silva
 */
public class WorkerWriter implements Runnable 
{
    private final Socket RequestSocket;
    private final PrintWriter SocketOutput;
    private final ClientsManager ClientsManager;
    private final AuctionsManager AuctionsManager;
    private String User;

    /**
     * 
     * @param RequestSocket The client socket this worker handles
     * @param ClientsManager The ClientsManager this worker delegates 
     * client-specific input to.
     * @param AuctionsManager The AuctionsManager this worker delegates
     * auctions-specific input to.
     * @param SocketOutput A Writer over a socket output stream.
     */
    public WorkerWriter(Socket RequestSocket, 
                        ClientsManager ClientsManager,
                        AuctionsManager AuctionsManager,
                        PrintWriter SocketOutput) 
    {
        this.RequestSocket=RequestSocket;
        this.ClientsManager=ClientsManager;
        this.AuctionsManager=AuctionsManager;
        this.SocketOutput=SocketOutput;
        User=null;
    }
    



    @Override
    public void run() 
    {
        //Debug String
        System.out.println("Worker Writer Start");
        if(handleLogin()) 
        {
            BlockingQueue<String> Log=ClientsManager.FetchClientLog(User);
            String CurrentString;
            while(!RequestSocket.isClosed()) 
            {
                try 
                {
                    CurrentString = Log.take();
                    SocketOutput.println(CurrentString);                      
                }
                catch (InterruptedException ex) {}
            }
        }
    }

    /**
     * HandleLogin works on the assumption that the client is also awaiting
     * the response on the login, otherwise messages may be lost since they
     * are not buffered or handled concurrently.
     * There could be asynchronous handling here as well but there doesn't
     * seem to us to be of much benefit.
     * @return a boolean indicating if a login did enter into effect
     */
    private boolean handleLogin() 
    {
        boolean SuccessfulLogin = false;
        SimpleQueue<String> ToWriteContainer
                = ClientsManager.getProcessorToWriterRequest(RequestSocket);
        String ToWrite = null;
        try 
        {
            while(!RequestSocket.isClosed() &&
                    !(ToWrite=ToWriteContainer.get()).equals("OK"))
            {
                SocketOutput.println(ToWrite);
            }
            if (!RequestSocket.isClosed()) 
            {
                SuccessfulLogin=true;
            }
        } 
        catch (InterruptedException ex) {}
        if (SuccessfulLogin) 
        {
            SimpleQueue<String> UserContainer
                = ClientsManager.getProcessorToWriterUser(RequestSocket);
            SocketOutput.println(ToWrite);
            try 
            {
                User=UserContainer.get();
            } 
            catch (InterruptedException ex) 
            {
                ex.printStackTrace();
            }
        }
        return SuccessfulLogin;
    }
    
}
