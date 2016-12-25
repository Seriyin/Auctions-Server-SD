/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Every AuctionsManager is a manager for the auctions in an auction house.
 * It manages accesses to all relevant auction data.
 * It must, with WorkerWriters, take care to ensure that disconnected clients 
 * will still be informed of relevant events when they reconnect
 * (such as the ending of auctions in which they participated).
 * @author Andre
 */
public class AuctionsManager extends Observable {
    private long currentAuctionNumber;
    private final Map<Long,Auction> Auctions;
    private final ExecutorService TaskPool;
    private final Map<String,PrintWriter> SocketOutputs;
    
    public AuctionsManager(ExecutorService TaskPool) {
        currentAuctionNumber=0;
        Auctions = new HashMap<>();
        SocketOutputs = new HashMap<>();
        this.TaskPool = TaskPool;
    }

    public void acknowledgeOutput(String User,PrintWriter SocketOutput)
    {
        synchronized(SocketOutputs) 
        {
            SocketOutputs.put(User,SocketOutput);
        }
    }

    public void socketDisconnected(String User) 
    {
        synchronized(SocketOutputs)
        {
            SocketOutputs.remove(User);
        }
    }
    
    public void handleAuctionInput(String User,
                                   String ToParse,
                                   ClientsManager ClientsManager) 
    {
        TaskPool.submit(()->handleInput(User,ToParse,ClientsManager));
    }
    
    private void handleInput(String User, 
                             String ToParse,
                             ClientsManager ClientsManager) 
    {
        Future<String> ResultString=
                TaskPool.submit(new WorkerInputHandler(User,ToParse,this));
        PrintWriter SocketOutput=null;
        synchronized(SocketOutputs)
        {
            if(SocketOutputs.containsKey(User))
                SocketOutput=SocketOutputs.get(User);
        }
        if (SocketOutput!=null) 
        {
            String StringToPrint=null;
            try 
            {
                StringToPrint=ResultString.get();
            }
            catch (InterruptedException | ExecutionException ex) 
            {
                ex.printStackTrace();
            }
            if (StringToPrint==null)
            {
                StringToPrint="Erro na leitura de texto";
            }
            TaskPool.submit(new WorkerLoglessWriter(StringToPrint,SocketOutput));
        }
    }
    
    public String listAuctions(String User) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String registerBid(long BidHash, float ValueToBid, String User) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String endAuction(String User, long AuctionCode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String registerAuction(String User, String Description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void removeAuction(long AuctionNumber) {
        synchronized(Auctions)
        {
            Auctions.remove(AuctionNumber);
        }
    }


}
