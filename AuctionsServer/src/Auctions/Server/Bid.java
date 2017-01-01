/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.Serializable;

/**
 *
 * @author Andre
 */
public class Bid implements Serializable
{
    private final String User;
    private float Bid;

    public Bid(String User,float Bid) 
    {
        this.User=User;
        this.Bid=Bid;
    }
    
    /**
     * Could happen that a user bids and a higher bid is processed first
     * @param Bid the bid to check if should be updated
     * @return if the bid was out of sequence.
     */
    public synchronized boolean updateBid(float Bid)
    {
        boolean IsLowerBid=false;
        if (Bid>this.Bid) 
        {
            this.Bid=Bid;
        }
        else 
        {
            IsLowerBid=true;
        }
        return IsLowerBid;
    }
    
    /**
     * Should be immutable.
     * @return User 
     */
    public String getUser() 
    {
        return User;
    }
    
    public synchronized float getBid() 
    {
        return Bid;
    }
    
    
}
