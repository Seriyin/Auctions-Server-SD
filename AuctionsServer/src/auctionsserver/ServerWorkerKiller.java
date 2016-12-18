/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auctionsserver;


import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andre
 */
public class ServerWorkerKiller implements Runnable 
{
    private final BlockingQueue<Thread> tll;
    
    public ServerWorkerKiller() 
    {
        tll=ThreadQueueSingleton.Instance();
    }
    
    @Override
    public void run() 
    {
        while (true) 
        {
            Thread t;
            try 
            {
                t = (Thread) tll.take();
                try 
                {
                    t.join();
                    //debug
                    System.out.println("Purged Thread: " + t.toString());
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
