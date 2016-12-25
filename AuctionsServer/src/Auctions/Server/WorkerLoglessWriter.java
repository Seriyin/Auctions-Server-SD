/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.PrintWriter;

/**
 * A simple Writer that writes non-important transient messages on request.
 * @author Andre
 */
public class WorkerLoglessWriter implements Runnable 
{
    private final String StringToPrint;
    private final PrintWriter SocketOutput;
    
    public WorkerLoglessWriter(String StringToPrint,
                               PrintWriter SocketOutput) 
    {
        this.StringToPrint=StringToPrint;
        this.SocketOutput=SocketOutput;
    }

    @Override
    public void run() 
    {
        SocketOutput.println(StringToPrint);
    }   
}
