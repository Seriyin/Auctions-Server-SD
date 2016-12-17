/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auctions;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Andre
 */
public final class ThreadQueueSingleton {
    private static final BlockingQueue<Thread> tll
            =new ArrayBlockingQueue<>(256);
    
    public final static BlockingQueue<Thread> Instance() 
    {
        return tll;
    }
}
