/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.util.concurrent.Callable;

/**
 * A Worker which handles parsing a unit of input (one single line of text)
 * from a socket input stream and instructing an AuctionsManager to handle
 * creating and writing a response.
 * @author André Diogo, Gonçalo Pereira, António Silva
 */
public class WorkerInputHandler implements Callable<String>
{
    private final String ToParse;
    private final AuctionsManager AuctionsManager;
    private final String User;
    //Currently not used but would indicate whether a string to possibly
    //be printed on the call should go to a client log.
    private final boolean ToLog;
    
    public WorkerInputHandler(String User,
                              String ToParse,
                              AuctionsManager AuctionsManager) 
    {
        this.User=User;
        this.ToParse=ToParse;
        this.AuctionsManager=AuctionsManager;
        ToLog=false;
    }
    
    @Override
    public String call() {
        String ResultString=null;
        String[] InputSplit = ToParse.trim().split("|");
        if (InputSplit.length>=1) 
        {
            //There is a command by a client 'C'
            if (InputSplit[0].equals("C")) 
            {
                //There is just the command, it's a request to read.
                if(InputSplit.length==1) 
                {
                    try
                    {
                        ResultString = AuctionsManager.listAuctions(User);
                    }
                    catch(NumberFormatException e){}                
                } 
                //There is a command and two arguments, it's a bid.
                else if(InputSplit.length==3)
                {
                    try
                    {
                        long AuctionCode=Long.parseLong(InputSplit[1]);
                        float ValueToBid=Float.parseFloat(InputSplit[2]);
                        ResultString = AuctionsManager.registerBid(AuctionCode,
                                                                   ValueToBid,
                                                                   User);
                    }
                    catch(NumberFormatException e){}
                }
            }
            //There is a command by a vendor.
            else if (InputSplit[0].equals("V")) 
            { 
                //If there is an argument try parsing an auction code
                //Otherwise it's an auction registration
                if(InputSplit.length==2) 
                {
                    try
                    {
                        long AuctionCode=Long.parseLong(InputSplit[1]);
                        ResultString=AuctionsManager.endAuction(User,
                                                                AuctionCode);
                    }
                    catch(NumberFormatException e) 
                    {
                        ResultString=
                                AuctionsManager.registerAuction(User,
                                                                InputSplit[1]);                        
                    }
                }                
            }
        }
        return ResultString;
    }


}
