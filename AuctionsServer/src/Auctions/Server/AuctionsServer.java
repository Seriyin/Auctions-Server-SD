/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author andre
 */
public class AuctionsServer {
    private final ServerSocket ServerSocket;
    private Socket CurrentSocket;
    private final ClientsManager AuctionsClientsManager;
    
    public AuctionsServer(int port) throws IOException 
    {
        ServerSocket=new ServerSocket(port);
        AuctionsClientsManager = new ClientsManager();
    }
    
    public void accept() throws IOException {
        CurrentSocket = ServerSocket.accept();
    }
    
    public Socket getSocket() {
        return CurrentSocket;
    }
    
        
    public void runThread() {
        Thread t;
        t = new Thread(new WorkerReader(CurrentSocket,AuctionsClientsManager));
        t.start();
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
                s.runThread();
                System.out.println("Run Thread");
            }
        }
        catch(IOException e) {
            System.out.println("Shit's whack yo");
        }
        // TODO code application logic here
    }
    
    
}
