/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Client;


import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Andre
 */
public class WorkerWriter implements Runnable {
    private final Socket RequestSocket;
    private final PrintWriter SocketOutput;
    private final BufferedReader SystemIn;
    private final PrintWriter SystemOut;


    public WorkerWriter(Socket RequestSocket, 
                        PrintWriter SocketOutput, 
                        BufferedReader SystemIn, 
                        PrintWriter SystemOut) 
    {

    }

    
    @Override
    public void run()
    {
    
    }
    
}
