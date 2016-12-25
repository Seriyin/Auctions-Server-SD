/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Every ClientsManager is a manager for the clients in an auction house.
 * It manages accesses to all relevant client data.
 * Keeps temporary logs for not yet authenticated sockets for exception writing.
 * Keeps ClientLogs as a repository for offline clients.
 * It implements Observer so it can be notified by a AuctionsManager of when
 * an auction ends.
 * @author Andre
 */
public class ClientsManager implements Observer
{
    //To be replaced by ??protobuffers??
    private final Map<String,String> Clients;
    private final Map<String,BlockingQueue<String>> ClientLogs;
    private final Map<Socket,BlockingQueue<String>> TempLogs;
    private final ExecutorService TaskPool;
    
    public ClientsManager(ExecutorService TaskPool) 
    {
        Clients = new HashMap<>(2048);
        ClientLogs = new HashMap<>();
        TempLogs = new HashMap<>();
        this.TaskPool = TaskPool;
    }
    
    /**
     * Gets the TemporaryLog for a given Socket.
     * @param RequestSocket
     * @return the corresponding TempLog.
     */
    public BlockingQueue<String> getTempLog(Socket RequestSocket)
    {
        synchronized(TempLogs) 
        {
           return TempLogs.get(RequestSocket);
        }
    }
    
    /**
     * Called by a factory to acknowledge that there is a new unauthenticated
     * user that needs a TempLog.
     * @param RequestSocket 
     */
    public void acknowledgeSocket(Socket RequestSocket) 
    {
        synchronized(TempLogs) 
        {
            TempLogs.put(RequestSocket, new ArrayBlockingQueue<>(64));
        }
    }
    

    /**
     * When an unauthenticated user disconnects
     * remove his TempLog.
     * @param User 
     */
    public void socketDisconnected(Socket RequestSocket)
    {
        synchronized(TempLogs) 
        {
            TempLogs.remove(RequestSocket);
        }
    }

    /**
     * Attempts to register a user,
     * otherwise writes to his TempLog what went wrong
     * @param User
     * @param Password
     * @param RequestSocket
     */
    public void registerUser(String User, String Password,
                         Socket RequestSocket) 
    {
        boolean ClientExists;
        BlockingQueue TempLog;
        synchronized(this.TempLogs)
        {
            TempLog=TempLogs.get(RequestSocket);
        }
        if (TempLog==null) 
        {
            try {
                RequestSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else 
        {
            synchronized(this.Clients) 
            {
                ClientExists=Clients.containsKey(User);
            }
            if (ClientExists) 
            {            
                try 
                {
                    //In deployment should have a localization layer.
                    TempLog.put("Utilizador já registado");
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                }
            }
            else 
            {
                synchronized(this.Clients)
                {
                    Clients.put(User, Password);
                }
                try 
                {
                    //In deployment should have a localization layer.
                    TempLog.put("Registo efetuado com sucesso");
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Attempts to login a user,
     * otherwise writes to his TempLog what went wrong
     * @param User
     * @param Password
     * @param RequestSocket
     * @return 
     */
    public boolean loginUser(String User, String Password, 
                      Socket RequestSocket) 
    {
        boolean SuccessfulLogin = true;
        boolean ClientExists;
        BlockingQueue TempLog;
        synchronized(this.TempLogs)
        {
            TempLog=TempLogs.get(RequestSocket);
        }
        if (TempLog==null) 
        {
            try {
                RequestSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            SuccessfulLogin=false;
        }
        else {
            synchronized(this.Clients) 
            {
                ClientExists=Clients.containsKey(User);
            }    
            if (!ClientExists) 
            {
                try 
                {
                    //In deployment should have a localization layer.
                   TempLog.put("Utilizador não existe");
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                }
                SuccessfulLogin=false;
            }
            else 
            { 
                String PasswordToMatch;
                synchronized(this.Clients) 
                {
                    PasswordToMatch=Clients.get(User);
                }
                if (PasswordToMatch.equals(Password)) 
                {
                    try 
                    {
                        //In deployment should have a localization layer.
                        TempLog.put("Password Incorreta");
                    }    
                    catch (InterruptedException ex) 
                    {
                        ex.printStackTrace();
                   }
                  SuccessfulLogin=false;
                }            
            }
            //On login register client as active and open an OutputStream
            if (SuccessfulLogin) 
            {
                synchronized(this.ClientLogs) 
                {
                    ClientLogs.put(User, new LinkedBlockingQueue<>(64));
                }
                try 
                {
                    //In deployment should have a localization layer.
                    TempLog.put("OK");
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                }
            }
        }
        return SuccessfulLogin;
    }

    /**
     * Update runs a new task to write to every ClientLog for each bidder in the
     * received Auction. 
     * @param o The Auctions Manager that is being observed
     * @param arg The Auction that just ended
     */
    @Override
    public void update(Observable o, Object arg) 
    {
        TaskPool.submit(()->WriteToClientLogs((AuctionsManager)o,(Auction)arg));
    }

    /**
     * Technically has consistency problems because someone can be
     * writing to the bidders when it iterates through other bidders
     * that aren't the highest at that point, but those who lose
     * won't be shown their losing bid so it doesn't matter.
     * @param AuctionsManager
     * @param Auction 
     */
    private void WriteToClientLogs(AuctionsManager AuctionsManager,
                                   Auction Auction) 
    {
        synchronized(Auction) 
        {
            Auction.flagInactive();
        }
        long AuctionNumber=Auction.getAuctionNumber();
        TreeSet<Bid> Bids=Auction.getBids();
        StringBuilder sb=new StringBuilder();
        String User;
        Bid HighestBid;    
        Future<BlockingQueue> Log;
        Future<?> WriteComputationResult;
        synchronized(Bids)
        {
            HighestBid = Bids.pollFirst();
            User=HighestBid.getUser();
            Log=TaskPool.submit(()->FetchClientLog(User));
            WriteComputationResult=
                TaskPool.submit(()->WriteToEachClient(Bids,
                                                      User,
                                                      AuctionNumber));
        }
        //Should check before updating if there is a need to notify
        //If it fails here it's an exception
        try 
        {
            sb.append("Ganhou o leilão ")
              .append(AuctionNumber)
              .append(", ")
              .append(User)
              .append(" com a proposta de ")
              .append(HighestBid.getBid());
            Log.get().add(sb.toString());
            WriteComputationResult.get();
            AuctionsManager.removeAuction(AuctionNumber);
        } 
        catch (InterruptedException 
                | ExecutionException | NullPointerException ex) 
        {
            ex.printStackTrace();
        }
        
    }

    /**
     * Fetch a ClientLog for a given User.
     * @param User
     * @return The Log
     */
    private BlockingQueue<String> FetchClientLog(String User) 
    {
        BlockingQueue<String> Log;
        synchronized(ClientLogs) 
        {
            Log=ClientLogs.get(User);
        }
        return Log;            
    }

    /**
     * 
     * @param Bids
     * @param HighestBidder
     * @param AuctionNumber 
     */
    private void WriteToEachClient(TreeSet<Bid> Bids,
                                   String HighestBidder,
                                   long AuctionNumber) 
    {
        final Future<String> ToLog;
        ToLog=TaskPool.submit(()->(CreateLosingAuctionString(HighestBidder,
                                                             AuctionNumber)));
        Bid CurrentBid;
        do {
            synchronized(Bids)
            {
                CurrentBid = Bids.pollFirst();
            }
            final Bid BidToTask=CurrentBid;
            TaskPool.submit(()->(WriteToClient(ToLog,BidToTask)));
        }
        while(CurrentBid!=null);
    }

    /**
     * Generates a String to output to all auction bidder who did no win.
     * @param HighestBidder
     * @param AuctionNumber
     * @return 
     */
    private String CreateLosingAuctionString(String HighestBidder, long AuctionNumber) {
        StringBuilder sb=new StringBuilder();                
        sb.append(HighestBidder)
          .append("ganhou o leilão ")
          .append(AuctionNumber)
          .append(", em que participou");
        return sb.toString();
    }

    /**
     * A simple task to write an Auction String to a bidder.
     * @param ToLog A string that is possibly 
     * being computed still asynchronously.
     * @param CurrentBid the Bid which contains the bidder to notify.
     */
    private void WriteToClient(Future<String> ToLog, Bid CurrentBid) 
    {
        try 
        {
            if (CurrentBid!=null) 
            {
                FetchClientLog(CurrentBid.getUser()).add(ToLog.get());
            }
        }
        catch (InterruptedException 
                | ExecutionException ex) 
        {
            ex.printStackTrace();
        }
    }


}
