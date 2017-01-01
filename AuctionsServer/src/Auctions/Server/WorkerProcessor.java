/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Andre
 */
public class WorkerProcessor implements Runnable 
{
    private final Socket RequestSocket;
    private final ClientsManager ClientsManager;
    private final AuctionsManager AuctionsManager;
    private final PrintWriter SocketOutput;
    private String User;
    
    public WorkerProcessor(Socket RequestSocket,
                           ClientsManager ClientsManager,
                           AuctionsManager AuctionsManager,
                           PrintWriter SocketOutput) 
    {
        this.RequestSocket=RequestSocket;
        this.ClientsManager=ClientsManager;
        this.AuctionsManager=AuctionsManager;
        this.SocketOutput=SocketOutput;
        this.User=null;
    }

    @Override
    public void run() 
    {
        //Debug String
        System.out.println("Worker Processor Start");
        if(handleLogin())
        {
            ClientsManager.cleanPreLoginLogs(RequestSocket);
            handleInput();
        }
    }

    private boolean handleLogin() 
    {

        SimpleQueue<LoginRequest> RequestContainer 
                = ClientsManager.getSocketToProcessorRequest(RequestSocket);
        SimpleQueue<String> ToWriteContainer
                = ClientsManager.getProcessorToWriterRequest(RequestSocket);
        SimpleQueue<String> UserContainer
                = ClientsManager.getProcessorToWriterUser(RequestSocket);
        SimpleQueue<Boolean> ResponseContainer
                = ClientsManager.getProcessorToSocketResponse(RequestSocket);
        boolean SuccessfulLogin=false;
        LoginRequest Request = null;
        while(!RequestSocket.isClosed() && !SuccessfulLogin)
        {
            Request=RequestContainer.get();
            if (Request.isLogin()) 
            {
                SuccessfulLogin=ClientsManager.loginUser(Request,
                                                         ToWriteContainer,
                                                         UserContainer);
                ResponseContainer.set(SuccessfulLogin);
            }
            else 
            {
                ClientsManager.registerUser(Request,ToWriteContainer);
            }
        }
        if (SuccessfulLogin) 
        {
            try 
            {
                User=Request.getUsername();
            }
            catch(NullPointerException e) 
            {
                try 
                {
                    RequestSocket.close();
                } 
                catch (IOException ex) 
                {
                    e.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        return RequestSocket.isClosed();
    }

    /**
     * Keep handling input while the reader is alive, when the reader dies
     * the Board is flushed and an END signifies a stop.
     */
    private void handleInput() 
    {
        BlockingQueue<String> ClientTaskBoard = 
                ClientsManager.FetchClientTaskBoard(User);
        String CurrentTask;
        try 
        {
            while(!RequestSocket.isClosed() &&
                  !(CurrentTask=ClientTaskBoard.take()).equals("END"))
            {
                AuctionsManager.handleAuctionInput(User, CurrentTask);
            }
        } 
        catch (InterruptedException e){}
    }
    
    
    
}
