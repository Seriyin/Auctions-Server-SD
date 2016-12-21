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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Every ClientsManager is a manager for an auction house.
 * It manages accesses to all relevant client data.
 * It keeps a pool of threads handy to instantiate
 * WorkerFetchers which are in charge of handling String construction
 * and which later defer socket writing to its own WorkerWriters.
 * It must take care to ensure that disconnected clients will still
 * be informed of relevant events when they reconnect(such as the ending 
 * of auctions in which they participated).
 * @author Andre
 */
public class ClientsManager {
    //To be replaced by ??protobuffers??
    private final Map<String,String> Clients;
    //For now keep both but the Sockets might not be necessary long-term
    //Keep the outputstreams to avoid creating them every time there is a
    //need to write.
    private final Map<String,Socket> ActiveClients;
    private final Map<String,PrintWriter> ActiveStreams;
    private final Map<Long,List<String>> Bidders;
    private final Map<Long,String> HighestBidders;
    private final ExecutorService WriterPool;
    
    public ClientsManager() 
    {
        ActiveClients = new HashMap<>();
        ActiveStreams = new HashMap<>();
        Bidders = new HashMap<>();
        HighestBidders = new HashMap<>();
        Clients = new HashMap<>();
        WriterPool = Executors.newFixedThreadPool(2048);
    }

    boolean registerUser(String User, String Password,
                         Socket RequestSocket, PrintWriter SocketOutput) 
    {
        boolean SuccessfulRegistration=true;
        synchronized(this.Clients) 
        {
            if (Clients.containsKey(User)) 
            {
                //In deployment should have a localization layer.
                WriterPool.submit(new WorkerWriter("Utilizador já registado",
                                                   SocketOutput));
                SuccessfulRegistration =false;
            }
            Clients.put(User, Password);
        }
        synchronized(this.ActiveClients) 
        {
            ActiveClients.put(User, RequestSocket);
        }
        synchronized(this.ActiveStreams)
        {
            ActiveStreams.put(User, SocketOutput);
        }        
        return SuccessfulRegistration;
    }

    boolean loginUser(String User, String Password, 
                      Socket RequestSocket, PrintWriter SocketOutput) 
    {
        boolean SuccessfulLogin = true;
        synchronized(this.Clients) 
        {
            if (!Clients.containsKey(User)) 
            {
                //In deployment should have a localization layer.
                WriterPool.submit(new WorkerWriter("Utilizador não existe",
                                                   SocketOutput));
                SuccessfulLogin=false;
            }
            else if (!Clients.get(User).equals(Password)) 
            {
                WriterPool.submit(new WorkerWriter("Password Incorreta",
                                                   SocketOutput));
                SuccessfulLogin=false;
            }
        }
        //On login register client as active and open an OutputStream
        if (SuccessfulLogin) 
        {
            synchronized(this.ActiveClients) 
            {
                ActiveClients.put(User, RequestSocket);
            }
            synchronized(this.ActiveStreams)
            {
                ActiveStreams.put(User, SocketOutput);
            }
        }
        return SuccessfulLogin;
    }

    void listClients(String User) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    void registerBid(long BidHash, float Value, String User) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void registerAuction(String User, String String) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void endAuction(String User, long AuctionCode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
