/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auctionsserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author andre
 */
public class AuctionsServer {
    private final ServerSocket s;
    private Socket sckt;
    private final ClientsManager clm;
    
    public AuctionsServer(int port) throws IOException 
    {
        s=new ServerSocket(port);
        clm = new ClientsManager();
    }
    
    public void accept() throws IOException {
        sckt = s.accept();
    }
    
    public Socket getSocket() {
        return sckt;
    }
    
    // maybe it will be useful, who knows
/*
    public void runMasterThread() {
        Thread t;
        t = new Thread(new AuctionsServerMaster(sckt,messages,clients,lclients));
        t.start();
        try {
            l.lock();
            tll.add(t);
        }
        finally {
            l.unlock();
        }
    }
*/
    
    private void runKiller() {
        Thread t=new Thread(new ServerWorkerKiller());
        t.start();
    }

    
    /**
     * Need to make these workers
     */
    public void runThreads() {
        Thread t;
        t = new Thread(new AuctionsServerWorkerReader(sckt,clm));
        t.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            AuctionsServer s=new AuctionsServer(9999);
//            s.runMasterThread();
            s.runKiller();
            while (true) {
                s.accept();
                s.runThreads();
                System.out.println("Run Thread");
            }
        }
        catch(IOException e) {
            System.out.println("Shit's whack yo");
        }
        // TODO code application logic here
    }
    
    
}
