/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.Serializable;
import java.util.TreeSet;

/**
 *
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
    

    public synchronized boolean isActive() 
    {
        return Active;
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
    
    public synchronized void addBid(Bid bid) {
        Bidders.add(bid);
    }
}
