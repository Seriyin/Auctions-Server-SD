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
    private MenuLogin MenuLogin;
    private MenuGlobal MenuGlobal;
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
            MenuLogin= new MenuLogin();
            MenuGlobal= new MenuGlobal();
            WorkerFactory.buildSocketWorkers(ClientSocket,MenuLogin,MenuGlobal);
            MenuLogin.bootMenuLogin();
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    
       
}