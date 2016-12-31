/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.PrintWriter;
import java.net.Socket;

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
    
    public WorkerProcessor(Socket RequestSocket,
                           ClientsManager ClientsManager,
                           AuctionsManager AuctionsManager,
                           PrintWriter SocketOutput) 
    {
        this.RequestSocket=RequestSocket;
        this.ClientsManager=ClientsManager;
        this.AuctionsManager=AuctionsManager;
        this.SocketOutput=SocketOutput;

    }

    @Override
    public void run() 
    {
        if(handleLogin())
        {
            
        }
    }

    private boolean handleLogin() 
    {
        SimpleQueue<LoginRequest> RequestContainer 
                = ClientsManager.getSocketToProcessorRequest(RequestSocket);
        SimpleQueue<String> ToWriteContainer
                = ClientsManager.getProcessorToWriterRequest(RequestSocket);
        SimpleQueue<Boolean> ResponseContainer
                = ClientsManager.getProcessorToSocketResponse(RequestSocket);
        boolean SuccessfulLogin=false;
        while(!RequestSocket.isClosed() && !SuccessfulLogin)
        {
            LoginRequest Request=RequestContainer.get();
            if (Request.isLogin()) 
            {
                SuccessfulLogin=ClientsManager.loginUser(Request,ToWriteContainer);
                ResponseContainer.set(SuccessfulLogin);
            }
            else 
            {
                ClientsManager.registerUser(Request,ToWriteContainer);
            }
        }
        return RequestSocket.isClosed();
    }
    
    
    
}
