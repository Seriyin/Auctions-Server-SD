/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author andre
 */
public class AuctionsServer {
    private final ServerSocket ServerSocket;
    private Socket CurrentSocket;
    private final ClientsManager ClientsManager;
    private final AuctionsManager AuctionsManager;
    private final WorkerFactory SocketWorkerFactory;
    
    public AuctionsServer(int port) throws IOException 
    {
        ServerSocket=new ServerSocket(port);
        ExecutorService TaskPool=Executors.newFixedThreadPool(2048);
        Map<String,PrintWriter> SharedSocketOutputs = new HashMap<>(); 
        ClientsManager = new ClientsManager(TaskPool,SharedSocketOutputs);
        AuctionsManager = new AuctionsManager(TaskPool,SharedSocketOutputs);
        AuctionsManager.addObserver(ClientsManager);
        SocketWorkerFactory = new WorkerFactory();
    }
    
    public void accept() throws IOException {
        CurrentSocket = ServerSocket.accept();
    }
    
    public Socket getSocket() {
        return CurrentSocket;
    }
    
        
    public void runHandlingThreads() {
        SocketWorkerFactory.buildSocketWorkers(CurrentSocket, 
                                               ClientsManager,
                                               AuctionsManager);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            AuctionsServer s=new AuctionsServer(9999);
//            s.runMasterThread();
            while (true) {
                s.accept();
                s.runHandlingThreads();
                System.out.println("Run Thread");
            }
        }
        catch(IOException e) {
            System.out.println("Shit's whack yo");
        }
        // TODO code application logic here
    }
    
    
}
