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
public class WorkerProcessor implements Runnable {

    public WorkerProcessor(Socket RequestSocket,
                           ClientsManager ClientsManager,
                           AuctionsManager AuctionsManager,
                           PrintWriter SocketOutput) 
    {
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
