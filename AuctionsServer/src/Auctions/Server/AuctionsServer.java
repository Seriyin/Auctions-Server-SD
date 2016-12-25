/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An AuctionsServer keeps a ClientsManager, an AuctionsManager 
 * and a WorkerFactory. It keeps accepting socket connections and then
 * asks it's factory to run Reader and Writer threads.
 * The Manager classes are unfortunately very tightly coupled.
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
        ClientsManager = new ClientsManager(TaskPool);
        AuctionsManager = new AuctionsManager(TaskPool);
        AuctionsManager.addObserver(ClientsManager);
        SocketWorkerFactory = new WorkerFactory();
    }
    
    public void accept() throws IOException {
        CurrentSocket = ServerSocket.accept();
    }
    
    public Socket getSocket() {
        return CurrentSocket;
    }
    
    /**
     * Asks the worker factory to build socket workers.
     */    
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
