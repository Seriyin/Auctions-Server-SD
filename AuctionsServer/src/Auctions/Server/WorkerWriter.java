/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auctionsserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Workers that exclusively write Strings to the corresponding socket.
 * @author Andre
 */
public class WorkerWriter implements Runnable {
    private final String ToWrite;
    private final Socket SocketToWrite;
    private PrintWriter SocketOutput;

    WorkerWriter(String ToWrite, Socket SocketToWrite) {
        this.ToWrite=ToWrite;
        this.SocketToWrite=SocketToWrite;
    }
    
    private PrintWriter initWriterToSocket(Socket SocketToWrite) 
    {
        OutputStream strm;
        try 
        {
            strm = SocketToWrite.getOutputStream();            
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
        if ((SocketOutput=initWriterToSocket(SocketToWrite))==null)
        {
            return;
        }
        /* get things from ClientsManager and write them out
           to socket here
        */
        try
        {
            SocketToWrite.close();
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    
}
