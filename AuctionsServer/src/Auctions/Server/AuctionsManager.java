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
 * Does this through use of the Observer/Observable pattern. It is being
 * observed by a clients manager and notifies it whenever it is necessary to
 * write to client logs in bulk, as is the case for an auction ending.
 * @author Andre Diogo, Gonçalo Pereira, António Silva
 */
public class AuctionsManager extends Observable 
{
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
    
    /**
     * Fetches a socket output for a given client in order to supply it
     * at runtime to it's corresponding workerwriter.
     * @param Username the client's username
     * @return a socket outputstream wrapped in a printwriter.
     */
    protected PrintWriter getSocketOutput(String Username) 
    {
        PrintWriter SocketOutput = null;
        synchronized(SocketOutputs)
        {
            if (SocketOutputs.containsKey(Username))
            {
                SocketOutput=SocketOutputs.get(Username);
            }
        }
        return SocketOutput;
    }

    /**
     * Called on successful login by the processor who registered said login,
     * To register an client socketoutputstream pair for later use.
     * @param User
     * @param SocketOutput 
     */
    public void acknowledgeOutput(String User,PrintWriter SocketOutput)
    {
        synchronized(SocketOutputs) 
        {
            SocketOutputs.put(User,SocketOutput);
        }
    }

    /**
     * when a socketDisconnects post authentication it is necessary to remove
     * its socket outputstream. By the time this method is called the socket
     * should already be closed.
     * @param User the User who disconnected.
     */
    public void atSocketDisconnected(String User) 
    {
        synchronized(SocketOutputs)
        {
            SocketOutputs.remove(User);
        }
    }
    
    public void handleAuctionInput(String User,
                                   String ToParse) 
    {
        TaskPool.submit(()->handleInput(User,ToParse));
    }
    
    private void handleInput(String User, 
                             String ToParse) 
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
            if (!StringToPrint.equals("END"))
            {
                TaskPool.submit(new WorkerLoglessWriter(StringToPrint,SocketOutput));
            }
        }
    }
    
    public String listAuctions(String User) 
    {
        String res= null;
        PrintWriter SocketOutput=getSocketOutput(User);
        synchronized(Auctions)
        {
            for (Auction value : Auctions.values()) 
            {
                res="END";
                if (value.isActive()) 
                {
                    //will hav 2 chang 500 to whatever is the
                    //sum of the limit of each string of the bid
                    StringBuilder sb=new StringBuilder(500);  
                    boolean isBidder;
                    isBidder=value.isBidder(User);
                    if (isBidder && User.equals(value.highestBidder())) 
                    {
                        sb.append("+");                   
                    }
                    else if (value.isAuctioneer(User))
                    {
                        sb.append("*");                           
                    }
                    sb.append(String.valueOf(value.getAuctionNumber()));
                    sb.append(value.getDescription());
                    sb.append("\n");
                    TaskPool.submit(new WorkerLoglessWriter(sb.toString(),SocketOutput));
                }
            }
        }    
        return res;
    }

    /**
     * Registers a bid only if the auction is still active at bid addition/replacing
     * time. If a bid is lower than a bid already registered, which may even happen
     * do to scheduling, informs the client of a bid being lower than a previously
     * registerd bid.
     * @param AuctionNumber the auction's identifier. 
     * @param ValueToBid the value to bid.
     * @param User the user who registers the bid.
     * @return a string indicating what happened with the bid.
     */
    public String registerBid(long AuctionCode, float ValueToBid, String User)
    {
        String Result;
        Bid b= new Bid(User,ValueToBid);
        Auction toRegisterBid=null;
        synchronized(Auctions) 
        {
            if(Auctions.containsKey(AuctionCode)) 
            {
                toRegisterBid = Auctions.get(AuctionCode);
            }
        }
        if (toRegisterBid==null) 
        {
            Result = "Leilão não existe";
        }
        else if(toRegisterBid.isAuctioneer(User)) 
        {
            Result = "Não pode licitar no seu próprio leilão";
        }
        else 
        {
            try 
            {
                if(toRegisterBid.addBid(b))
                {
                    Result = "Licitação registada";
                }
                else
                {
                    Result= "Já existe uma licitação de valor superior";
                }
            } 
            catch (InactiveAuctionException ex) 
            {
                Result=ex.message();
            }
        }
        return Result;
    }

    /**
     * Checks if a User is the auctioneer in an auction identified by the
     * AuctionCode and if so, rings a call to notify the ClientsManager that 
     * there is a need to write to people's logs.
     * @param User the user who is trying to end an auction
     * @param AuctionCode the auction's identifier
     * @return a dummy string for success or an error string.
     */
    public String endAuction(String User, long AuctionCode) 
    {
        String ResultString;
        Auction toEnd=null;
        synchronized(Auctions) 
        {
            if(Auctions.containsKey(AuctionCode)) 
            {
                toEnd=Auctions.get(AuctionCode);
            }
        }
        if (toEnd==null) 
        {
            ResultString="Leilão não existe";
        }
        else if (!toEnd.isAuctioneer(User))
        {
            ResultString="O leilão não é seu";
        }
        else if (toEnd.isActive())
        {
            try
            {
                this.setChanged();
                this.notifyObservers(toEnd);
                ResultString="END";
            }
            catch(InactiveAuctionException e) 
            {
                this.clearChanged();
                ResultString=e.message();
            }
        }
        else 
        {
            ResultString="Leilão já terminado";
        }
        return ResultString;
    }

    /**
     * For now we assume a Description is one line only.
     * In principle a new auction should always register.
     * @param User the user who wishes to register an auction
     * @param Description the auction's description.
     * @return a String to print to said user.
     */
    public String registerAuction(String User, String Description) {
        Auction a= new Auction(User,Description,generateAuctionNumber());
        synchronized(Auctions)
        {
            Auctions.put(a.getAuctionNumber(),a);
        }
        return "Novo leilão registado com sucesso";
    }

    /**
     * Called only from the end of an auction, in principle should always
     * work because an auction is flagged as inactive and from that point can
     * only be handled by one instance of an auction ending.
     * @param AuctionNumber 
     */
    public void removeAuction(long AuctionNumber) 
    {
        synchronized(Auctions)
        {
            if(Auctions.containsKey(AuctionNumber)) 
            {
                Auctions.remove(AuctionNumber);
            }
        }
    }
    
    public synchronized long generateAuctionNumber (){
        return (++this.currentAuctionNumber);
    }

}
