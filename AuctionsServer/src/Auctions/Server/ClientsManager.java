/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

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
    //To be replaced by protobuffers
    private final Map<String,String> Clients;
    private final Map<String,Socket> ActiveClients;
    private final Map<Long,List<String>> Bidders;
    private final Map<Long,String> HighestBidders;
    private final ExecutorService WriterPool;
    
    public ClientsManager() 
    {
        ActiveClients = new HashMap<>();
        Bidders = new HashMap<>();
        HighestBidders = new HashMap<>();
        Clients = new HashMap<>();
        WriterPool = Executors.newFixedThreadPool(2048);
    }

    boolean registerUser(String User, String Password,Socket RequestSocket) 
    {
        boolean SuccessfulRegistration=true;
        synchronized(this.Clients) 
        {
            if (Clients.containsKey(User)) 
            {
                //In deployment should have a localization layer.
                WriterPool.submit(new WorkerWriter("Utilizador já registado",RequestSocket));
                SuccessfulRegistration =false;
            }
            Clients.put(User, Password);
        }
        synchronized(this.ActiveClients) 
        {
            ActiveClients.put(User, RequestSocket);
        }
        return SuccessfulRegistration;
    }

    boolean loginUser(String User, String Password,Socket RequestSocket) 
    {
        boolean SuccessfulLogin = true;
        synchronized(this.Clients) 
        {
            if (!Clients.containsKey(User)) 
            {
                //In deployment should have a localization layer.
                WriterPool.submit(new WorkerWriter("Utilizador não existe",RequestSocket));
                SuccessfulLogin=false;
            }
            else if (!Clients.get(User).equals(Password)) 
            {
                WriterPool.submit(new WorkerWriter("Password Incorreta",RequestSocket));
                SuccessfulLogin=false;
            }
        }
        if (SuccessfulLogin) 
        {
            synchronized(this.ActiveClients) 
            {
                ActiveClients.put(User, RequestSocket);
            }
        }
        return SuccessfulLogin;
    }

    void listClients(String User) {
        
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
