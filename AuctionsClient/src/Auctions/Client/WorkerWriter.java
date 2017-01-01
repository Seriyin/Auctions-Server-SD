/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Client;


import Auctions.UI.InputProcedure;
import Auctions.UI.Menu;
import Auctions.Util.Wrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Andre
 */
public class WorkerWriter implements Runnable 
{
    private final Socket RequestSocket;
    private final PrintWriter SocketOutput;
    private final BufferedReader SystemIn;
    private final PrintWriter SystemOut;
    private final Wrapper<String> SharedString;


    public WorkerWriter(Socket RequestSocket, 
                        PrintWriter SocketOutput, 
                        BufferedReader SystemIn, 
                        PrintWriter SystemOut,
                        Wrapper<String> SharedString) 
    {
        this.RequestSocket=RequestSocket;
        this.SocketOutput=SocketOutput;
        this.SystemIn=SystemIn;
        this.SystemOut=SystemOut;
        this.SharedString=SharedString;
    }

    
    @Override
    public void run()
    {
        Menu m;
        {
            StringBuilder sb=new StringBuilder(500);
            sb.append("0 - Sair");
            sb.append("1 - Login");
            sb.append("2 - Registar");
            m = new Menu(sb.toString(),SystemIn);
        }
        m.addChoice((InputProcedure)this::LoginUser);
        m.addChoice((InputProcedure)this::RegisterUser);        
        try 
        {
            m.run();
        } 
        catch (IOException ex) {}
    }
    
    private void LoginUser(BufferedReader bf) throws IOException
    {
        SocketOutput.println("0");
        SystemOut.println("Digite o seu nome de utilizador");
        SocketOutput.println(Menu.readString(bf));
        SystemOut.println("Digite a sua password");
        SocketOutput.println(Menu.readString(bf));
        String Result=null;
        while(!(Result=SharedString.get()).equals("OK") || !Result.equals("NOT")) 
        {
            try 
            {
                SharedString.wait();
            } 
            catch (InterruptedException ex) 
            {
                ex.printStackTrace();
            }
        }
        if(Result.equals("OK")) 
        {
            MainMenu();
        }
    }

    private void RegisterUser(BufferedReader bf) throws IOException
    {
        SocketOutput.println("1");
        SystemOut.println("Digite o seu nome de utilizador");
        SocketOutput.println(Menu.readString(bf));
        SystemOut.println("Digite a sua password");
        SocketOutput.println(Menu.readString(bf));
    }
    
    private void MainMenu() 
    {
        Menu m;
        {
            StringBuilder sb=new StringBuilder(500);
            sb.append("0 - Sair");
            sb.append("1 - Listar Leilões");
            sb.append("2 - Registar Leilão");
            sb.append("3 - Licitar Leilão");
            sb.append("4 - Terminar Leilão");
            m = new Menu(sb.toString(),SystemIn);
        }
        m.addChoice((InputProcedure)this::ListAuctions);
        m.addChoice((InputProcedure)this::RegisterAuction);
        m.addChoice((InputProcedure)this::BidAuction);
        m.addChoice((InputProcedure)this::EndAuction);
    }
    
    private void ListAuctions(BufferedReader bf) throws IOException
    {
    }
    
    private void RegisterAuction(BufferedReader bf) throws IOException
    {
    }
    
    private void BidAuction(BufferedReader bf) throws IOException
    {
    }
    
    private void EndAuction(BufferedReader bf) throws IOException
    {
    }
    
}
