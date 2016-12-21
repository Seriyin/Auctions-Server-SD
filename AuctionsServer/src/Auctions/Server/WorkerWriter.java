/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

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
    private final PrintWriter SocketOutput;

    WorkerWriter(String ToWrite, PrintWriter SocketOutput) {
        this.ToWrite=ToWrite;
        this.SocketOutput=SocketOutput;
    }
    

    @Override
    public void run() 
    {
        //Debug String
        System.out.println("Worker Writer Start");
        SocketOutput.println(ToWrite);
    }
    
}
