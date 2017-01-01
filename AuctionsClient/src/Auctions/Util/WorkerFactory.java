/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import Auctions.Client.WorkerReader;
import Auctions.Client.WorkerWriter;

/**
 * A worker factory contains an expandable thread pool
 * to 'infinitely' run new WorkerThreads for dedicated reading
 * and writing, makes it vulnerable to slow loris.
 * It tries to initialize input and output streams for each socket
 * before running WorkerThreads in order to be fail-fast.
 * If a socket goes bad it will necessarily be after submitting new workers.
 * @author André Diogo, Gonçalo Pereira, António Silva
 */
public class WorkerFactory 
{
    public static void buildSocketWorkers(Socket RequestSocket) 
    {
        BufferedReader SocketInput = initReaderFromSocket(RequestSocket);
        PrintWriter SocketOutput = initWriterToSocket(RequestSocket);
        BufferedReader SystemIn = initSystemReader();
        PrintWriter SystemOut = initSystemWriter();
        Wrapper<String> SharedString=new Wrapper();
        if (SocketInput != null && SocketOutput !=null &&
            SystemIn!=null && SystemOut!=null) 
        {
            Thread Reader=new Thread (new WorkerReader(RequestSocket,
                                                       SocketInput,
                                                       SystemOut,
                                                       SharedString));
            Thread Writer=new Thread (new WorkerWriter(RequestSocket,
                                                       SocketOutput,
                                                       SystemIn,
                                                       SystemOut,
                                                       SharedString));
            Reader.start();
            Writer.start();
        }
    }
    
    private static BufferedReader initReaderFromSocket(Socket SocketToRead) 
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
    
    private static PrintWriter initWriterToSocket(Socket SocketToWrite) 
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

    private static BufferedReader initSystemReader() 
    {
        return new BufferedReader(new InputStreamReader(System.in));
    }

    private static PrintWriter initSystemWriter() 
    {
        return new PrintWriter(System.out);
    }

}
