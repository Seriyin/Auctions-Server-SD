/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Right now we have to be very careful.
 * Auctions has nested synchronization.
 * Will have to think about it more tomorrow.
 * @author Andre Diogo, Gonçalo Pereira, António Silva
 */
public class Auction implements Serializable {
    private final long AuctionNumber;
    private boolean Active;
    private final String Auctioneer;
    private final String Description;
    private final TreeSet<Bid> Bidders;

    public long getAuctionNumber()
    {
        return AuctionNumber;
    }
    
    /**
     * The Auctioneer should be immutable.
     * @return the auction's auctionner.
     */
    public String getAuctioneer() 
    {
        return Auctioneer;
    }
    
    public boolean isAuctioneer(String Username)
    {
        return Username.equals(Auctioneer);
    }

    public synchronized boolean isActive() 
    {
        return Active;
    }

    public synchronized void flagInactive() {
        Active=false;
    }
    
    /**
     * The Description should be immutable
     * @return the auction's description.
     */
    public String getDescription() 
    {
        return Description;
    }

    public synchronized TreeSet<Bid> getBids() 
    {
        return Bidders;
    }    
    
    public Auction(String User, String Description,long AuctionNumber) 
    {
        Active=true;
        this.AuctionNumber=AuctionNumber;
        this.Auctioneer=User;
        this.Description=Description;
        this.Bidders=new TreeSet<>(
                (bid1,bid2)->{if (bid1.getBid()>bid2.getBid()) return 1;
                              else if(bid1.getBid()==bid2.getBid()) return 0;
                              else return -1;});
    }


    /**
     * A poll for highestBidder should only synchronize on the bidders.
     * @return the user who is the highest bidder.
     */
    public String highestBidder()
    {
        Bid HighestBid=null;
        String Result=null;
        synchronized(Bidders) 
        {
            if (!Bidders.isEmpty()) 
            {
                HighestBid=Bidders.first();
            }
        }
        if (HighestBid!=null)
        {
            Result=HighestBid.getUser();
        }
        return Result;
    }
    
    /**
     * Fetches a User's current bid, if he has one
     * @param User the user to get his bid.
     * @return his most current bid.
     */
    public Bid getBid(String User) 
    {
        synchronized (Bidders) 
        {
            return Bidders.stream()
                          .filter(b->b.getUser().equals(User))
                          .findFirst()
                          .orElse(null);
        }
    }
    
    /**
     * Returns an indication of if the bid was lower than one already
     * registered for the given user.
     * Synchronizes on the auction so no one can flag a bid as inactive
     * until a bid has been finished.
     * @param bid The bid to register.
     * @return if the bid was lower.
     * @throws Auctions.Server.InactiveAuctionException in case an auction
     * as already finished.
     */
    public synchronized boolean addBid(Bid bid) throws InactiveAuctionException 
    {
        boolean LowerBid = false;
        if(isActive()) 
        {
            Bid ExistingBid=getBid(bid.getUser());
            if (ExistingBid==null) 
            {
                synchronized(Bidders) 
                {
                    Bidders.add(bid);
                }                
            }
            else 
            {
                LowerBid=ExistingBid.updateBid(bid.getBid());
            }
        }
        else 
        {
            throw new InactiveAuctionException();
        }
        return LowerBid;
    }
    
    public boolean isBidder(String User) 
    {
        synchronized(Bidders) 
        {
            return Bidders.stream().anyMatch(b->b.getUser().equals(User));
        }
    }

    /**
     * HighestBid polls the first bid. It is gone after.
     * @return 
     */
    public Bid highestBid() 
    {
        synchronized(Bidders) 
        {
            return Bidders.pollFirst();
        }
    }
}
