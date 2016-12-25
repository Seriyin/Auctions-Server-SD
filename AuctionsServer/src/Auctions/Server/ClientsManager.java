/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Every ClientsManager is a manager for the clients in an auction house.
 * It manages accesses to all relevant client data.
 * Keeps temporary logs for not yet authenticated sockets for exception writing.
 * @author Andre
 */
public class ClientsManager 
{
    //To be replaced by ??protobuffers??
    private final Map<String,String> Clients;
    private final Map<String,Socket> ActiveSockets;
    private final Map<String,BlockingQueue<String>> ClientLogs;
    private final Map<Socket,BlockingQueue<String>> TempLogs;
    private final ExecutorService TaskPool;
    private final Map<String,PrintWriter> SharedSocketOutputs;
    
    public ClientsManager(ExecutorService TaskPool,
                          Map<String,PrintWriter> SharedSocketOutputs) 
    {
        ActiveSockets = new HashMap<>(256);
        Clients = new HashMap<>(2048);
        ClientLogs = new HashMap<>();
        TempLogs = new HashMap<>();
        this.TaskPool = TaskPool;
        this.SharedSocketOutputs = SharedSocketOutputs;
    }
    
    public BlockingQueue<String> getTempLog(Socket RequestSocket)
    {
        synchronized(TempLogs) 
        {
           return TempLogs.get(RequestSocket);
        }
    }
    
    public void acknowledgeSocket(Socket RequestSocket) 
    {
        synchronized(TempLogs) 
        {
            TempLogs.put(RequestSocket, new ArrayBlockingQueue<>(64));
        }
    }
    

    public void socketDisconnected(String User)
    {
        synchronized(this.ActiveSockets) 
        {
            ActiveSockets.remove(User);
        }
    }

    
    public boolean registerUser(String User, String Password,
                         Socket RequestSocket) 
    {
        boolean SuccessfulRegistration=true;
        boolean ClientExists;
        BlockingQueue TempLog;
        synchronized(this.TempLogs)
        {
            TempLog=TempLogs.get(RequestSocket);
        }
        if (TempLog==null) 
        {
            try {
                RequestSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            SuccessfulRegistration=false;
        }
        else 
        {
            synchronized(this.Clients) 
            {
                ClientExists=Clients.containsKey(User);
            }
            if (ClientExists) 
            {            
                try 
                {
                    //In deployment should have a localization layer.
                    TempLog.put("Utilizador já registado");
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                }
                SuccessfulRegistration =false;
            }
            else 
            {
                synchronized(this.Clients)
                {
                    Clients.put(User, Password);
                }
                try 
                {
                    //In deployment should have a localization layer.
                    TempLog.put("Registo efetuado com sucesso");
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                }
            }
        }
        return SuccessfulRegistration;
    }

    
    public boolean loginUser(String User, String Password, 
                      Socket RequestSocket) 
    {
        boolean SuccessfulLogin = true;
        boolean ClientExists;
        BlockingQueue TempLog;
        synchronized(this.TempLogs)
        {
            TempLog=TempLogs.get(RequestSocket);
        }
        if (TempLog==null) 
        {
            try {
                RequestSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            SuccessfulLogin=false;
        }
        else {
            synchronized(this.Clients) 
            {
                ClientExists=Clients.containsKey(User);
            }    
            if (!ClientExists) 
            {
                try 
                {
                    //In deployment should have a localization layer.
                   TempLog.put("Utilizador não existe");
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                }
                SuccessfulLogin=false;
            }
            else 
            { 
                String PasswordToMatch;
                synchronized(this.Clients) 
                {
                    PasswordToMatch=Clients.get(User);
                }
                if (PasswordToMatch.equals(Password)) 
                {
                    try 
                    {
                        //In deployment should have a localization layer.
                        TempLog.put("Password Incorreta");
                    }    
                    catch (InterruptedException ex) 
                    {
                        ex.printStackTrace();
                   }
                  SuccessfulLogin=false;
                }            
            }
            //On login register client as active and open an OutputStream
            if (SuccessfulLogin) 
            {
                synchronized(this.ActiveSockets) 
                {
                    ActiveSockets.put(User, RequestSocket);
                }
                synchronized(this.ClientLogs) 
                {
                    ClientLogs.put(User, new LinkedBlockingQueue<>(64));
                }
                try 
                {
                    //In deployment should have a localization layer.
                    TempLog.put("OK");
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                }
            }
        }
        return SuccessfulLogin;
    }


}
