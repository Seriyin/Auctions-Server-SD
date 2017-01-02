/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;


/**
 * A simple BlockingQueue which supports only blocking get and a set.
 * @author André Diogo, Gonçalo Pereira, António Silva
 * @param <T> The single object to store in the queue
 */
public final class SimpleQueue<T> 
{
    private T object;
    
    public SimpleQueue() 
    {
        object=null;
    }
    
    public SimpleQueue(T object)
    {
        this.object=object;
    }
    
    public synchronized boolean isEmpty() 
    {
        return object==null;
    }
    
    public synchronized void set(T object) 
    {
        this.object=object;
        notify();
    }
    
    public synchronized T get() throws InterruptedException 
    {
        while (isEmpty()) 
        { 
            wait();
        } 
        T result=object;
        object=null;
        return result;
    }
}
