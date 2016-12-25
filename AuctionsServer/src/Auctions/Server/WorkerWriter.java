/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * Workers that write Strings from a client's log to the corresponding socket.
 * A client's log contains only important information that must remain even
 * when offline.
 * Workers begin by writing from TempLogs on a corresponding socket before
 * authentication.
 * @author Andre
 */
public class WorkerWriter implements Runnable 
{
    private final Socket SocketToWrite;
    private final PrintWriter SocketOutput;
    private final ClientsManager ClientsManager;
    private final AuctionsManager AuctionsManager;

    WorkerWriter(Socket SocketToWrite, 
                 ClientsManager ClientsManager,
                 AuctionsManager AuctionsManager,
                 PrintWriter SocketOutput) 
    {
        this.SocketToWrite=SocketToWrite;
        this.ClientsManager=ClientsManager;
        this.AuctionsManager=AuctionsManager;
        this.SocketOutput=SocketOutput;
    }
    



    @Override
    public void run() 
    {
        //Debug String
        System.out.println("Worker Writer Start");
        /**
         * Implement here
         */
    }
    
}
