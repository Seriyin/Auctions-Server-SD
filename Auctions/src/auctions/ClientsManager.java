/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auctions;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Every ClientsManager is a manager for an auction house.
 * It manages accesses to all relevant client data
 * and also writer threads' queues of messages to write out
 * to clients.
 * @author Andre
 */
public class ClientsManager {
    private final Map<String,String> clients;
    private final Map<String,Socket> activeClients;
    private final Map<Long,List<String>> bidders;
    private final Map<Long,String> highestBidders;
    private final Map<String,BlockingQueue<String>> writeQueues;
    
    public ClientsManager() 
    {
        activeClients = new HashMap<>();
        bidders = new HashMap<>();
        highestBidders = new HashMap<>();
        clients = new HashMap<>();
        writeQueues=new HashMap<>();
    }

    boolean registerUser(String user, String password) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean loginUser(String user, String password) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void listClients(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void registerBid(long hash, float value, String user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void registerAuction(String user, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
