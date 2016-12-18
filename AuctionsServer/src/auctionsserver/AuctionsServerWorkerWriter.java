/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auctionsserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Andre
 */
public class AuctionsServerWorkerWriter implements Runnable {
    private final String user;
    private final ClientsManager clm;
    private final Socket sckt;
    private PrintWriter pw;

    AuctionsServerWorkerWriter(String user, ClientsManager clm, Socket sckt) {
        this.user=user;
        this.clm=clm;
        this.sckt=sckt;
    }
    
    private PrintWriter initWriterToSocket(Socket sckt) 
    {
        OutputStream strm;
        try 
        {
            strm = sckt.getOutputStream();            
        }
        catch(IOException e) 
        {
            return null;
        }
        return new PrintWriter(strm);
    }

    @Override
    public void run() 
    {
        System.out.println("Worker Writer Start");
        if ((pw=initWriterToSocket(sckt))==null)
        {
            return;
        }
        /* get things from ClientsManager and write them out
           to socket here
        */
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
    
}
