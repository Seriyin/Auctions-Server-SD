/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A worker factory contains an expandable thread pool
 * to 'infinitely' run new WorkerThreads for dedicated reading
 * and writing, makes it vulnerable to slow loris.
 * It tries to initialize input and output streams for each socket
 * before running WorkerThreads in order to be fail-fast.
 * If a socket goes bad it will necessarily be after submitting new workers.
 * @author Andre
 */
public class WorkerFactory {
    private final ExecutorService ExpandableThreadPool;
    
    public WorkerFactory()
    {
        ExpandableThreadPool = Executors.newCachedThreadPool();
    }
    
    public void buildSocketWorkers(Socket RequestSocket, 
                                   ClientsManager ClientsManager,
                                   AuctionsManager AuctionsManager) 
    {
        BufferedReader SocketInput = initReaderFromSocket(RequestSocket);
        PrintWriter SocketOutput = initWriterToSocket(RequestSocket);
        if (SocketInput != null && SocketOutput !=null) 
        {
            ClientsManager.acknowledgeSocket(RequestSocket);
            ExpandableThreadPool.submit(new WorkerReader(RequestSocket,
                                                         ClientsManager,
                                                         AuctionsManager,
                                                         SocketInput));
            ExpandableThreadPool.submit(new WorkerWriter(RequestSocket,
                                                         ClientsManager,
                                                         AuctionsManager,
                                                         SocketOutput));
            ExpandableThreadPool.submit(new WorkerProcessor(RequestSocket,
                                                            ClientsManager,
                                                            AuctionsManager,
                                                            SocketOutput));

        }
    }
    
    private BufferedReader initReaderFromSocket(Socket SocketToRead) 
    {
        InputStream strm;
        try 
        {
            strm = SocketToRead.getInputStream();            
        }
        catch(IOException e) 
        {
            return null;
        }
        return new BufferedReader
                        (new InputStreamReader(strm));
    }
    
    private PrintWriter initWriterToSocket(Socket SocketToWrite) 
    {
        OutputStream strm;
        try 
        {
            strm = SocketToWrite.getOutputStream();            
        }
        catch(IOException e) 
        {
            return null;
        }
        return new PrintWriter(strm);
    }

}
