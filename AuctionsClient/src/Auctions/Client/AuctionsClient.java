/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Client;

import Auctions.GUI.MenuGlobal;
import Auctions.GUI.MenuLogin;
import Auctions.Util.WorkerFactory;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author André Diogo, Gonçalo Pereira, António Silva
 */
public class AuctionsClient 
{
    private Socket ClientSocket;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        AuctionsClient Client=new AuctionsClient();
    }
    
    public AuctionsClient()
    {
        try 
        {
            ClientSocket= new Socket("localhost",9999);
            WorkerFactory.buildSocketWorkers(ClientSocket);
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    
       
}