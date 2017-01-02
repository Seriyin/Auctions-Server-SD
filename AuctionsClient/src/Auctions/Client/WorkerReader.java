/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Client;


import Auctions.Util.Wrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Andre
 */
public class WorkerReader implements Runnable 
{
    private final Socket RequestSocket;
    private final BufferedReader SocketInput;
    private final Wrapper<String> SharedString;

    public WorkerReader(Socket RequestSocket, 
                        BufferedReader SocketInput, 
                        Wrapper<String> SharedString) 
    {
        this.RequestSocket=RequestSocket;
        this.SocketInput=SocketInput;
        this.SharedString=SharedString;
    }


    @Override
    public void run() 
    {
        try 
        {
            if(!handleLogin())
            {
                handleInput();
            }
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
        try 
        {
            RequestSocket.close();
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }

    private boolean handleLogin() throws IOException
    {
        boolean BlewUp=false;
        String ServerResponse=SocketInput.readLine();
        while((ServerResponse)!=null 
               && !ServerResponse.equals("OK"))
        {
            if (ServerResponse.equals("NOT")) 
            {
                SharedString.set(ServerResponse);
                System.out.println("Login inválido");
            }
            else 
            {
                System.out.println(ServerResponse);
            }
            ServerResponse=SocketInput.readLine();
        }
        if (ServerResponse==null) 
        {
            BlewUp=true;   
        }
        else
        {
            SharedString.set(ServerResponse);
        }
        return BlewUp;
    }

    private void handleInput() throws IOException
    {
        String Response;
        while((Response=SocketInput.readLine())!=null) 
        {
            System.out.println(Response);
        }
    }
    
}
