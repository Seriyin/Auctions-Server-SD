/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Client;

import Auctions.GUI.MenuGlobal;
import Auctions.GUI.MenuLogin;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Andre
 */
public class WorkerWriter implements Runnable {

    public WorkerWriter(Socket RequestSocket, BufferedReader SocketInput, MenuLogin MenuLogin, MenuGlobal MenuGlobal) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void run()
    {
    
    }
    
}
