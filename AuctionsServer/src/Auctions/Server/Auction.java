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
 * @author Andre
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
    
    public synchronized void setInnactive() 
    {
        this.Active=false; 
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

    public synchronized void flagInactive() {
        Active=false;
    }
    
    public synchronized String highestBid(){
        return (this.Bidders.first()).getUser();
    }
    
    /**
     * Returns an indication of if the bid was lower than one already
     * registered for the given user.
     * @param bid The bid to register
     * @return if the bid was lower
     */
    public synchronized boolean addBid(Bid bid) 
    {
        boolean LowerBid = false;
        Bid ExistingBid=null;
        synchronized (Bidders) 
        {
            for(Bid b : Bidders) 
            {
                if (b.getUser().equals(bid.getUser()))
                {
                    ExistingBid=b;
                    break;
                }
            }
        }
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
        return LowerBid;
    }
    
    public synchronized boolean isBidder(String User) 
    {
        synchronized(Bidders) 
        {
            return Bidders.stream().anyMatch(b->b.getUser().equals(User));
        }
    }
}
