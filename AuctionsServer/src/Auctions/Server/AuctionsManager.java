/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
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
    
    public List<String> listAuctions(String User) {
        List<String> res= null;
        StringBuilder sb=new StringBuilder(500); //will hav 2 chang 500 to whatever is the sum of the limit of each string of the bid 
         synchronized(Auctions)
        {
            for (Auction value : Auctions.values()) {
                if (value.getBids().contains(User)||User.equals(value.getAuctioneer())) {
                    if (User.equals(value.highestBid())) {
                        sb.append("+");                   
                    }else{
                        if (User.equals(value.getAuctioneer())){
                            sb.append("*");                           
                        }
                    }
                    sb.append(String.valueOf(value.getAuctionNumber()));
                    sb.append(value.getDescription());
                    sb.append("\n");
                }
                res.add(sb.toString());
            }            
        }    
        return res;
    }

    public String registerBid(long BidHash, float ValueToBid, String User) {
        Bid b= new Bid(User,ValueToBid);
        Auctions.get(currentAuctionNumber).addBid(b);
        return "Licitação refistada";
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
