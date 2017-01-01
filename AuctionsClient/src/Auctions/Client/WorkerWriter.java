/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Client;


import Auctions.UI.InputProcedure;
import Auctions.UI.Menu;
import Auctions.UI.Procedure;
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
    private final Wrapper<String> SharedString;


    public WorkerWriter(Socket RequestSocket, 
                        PrintWriter SocketOutput, 
                        BufferedReader SystemIn, 
                        Wrapper<String> SharedString) 
    {
        this.RequestSocket=RequestSocket;
        this.SocketOutput=SocketOutput;
        this.SystemIn=SystemIn;
        this.SharedString=SharedString;
    }

    
    @Override
    public void run()
    {
        Menu m;
        {
            StringBuilder sb=new StringBuilder(500);
            sb.append("0 - Sair\n");
            sb.append("1 - Login\n");
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
        System.out.println("Digite o seu nome de utilizador");
        SocketOutput.println(Menu.readString(bf));
        System.out.println("Digite a sua password");
        SocketOutput.println(Menu.readString(bf));
        String Result=null;
        while((Result=SharedString.get())==null);
        if(Result.equals("OK")) 
        {
            MainMenu();
        }
    }

    private void RegisterUser(BufferedReader bf) throws IOException
    {
        SocketOutput.println("1");
        System.out.println("Digite o seu nome de utilizador");
        SocketOutput.println(Menu.readString(bf));
        System.out.println("Digite a sua password");
        SocketOutput.println(Menu.readString(bf));
    }
    
    private void MainMenu() 
    {
        Menu m;
        {
            StringBuilder sb=new StringBuilder(500);
            sb.append("0 - Sair\n");
            sb.append("1 - Listar Leilões\n");
            sb.append("2 - Registar Leilão\n");
            sb.append("3 - Licitar Leilão\n");
            sb.append("4 - Terminar Leilão");
            m = new Menu(sb.toString(),SystemIn);
        }
        m.addChoice((Procedure)this::ListAuctions);
        m.addChoice((InputProcedure)this::RegisterAuction);
        m.addChoice((InputProcedure)this::BidAuction);
        m.addChoice((InputProcedure)this::EndAuction);
    }
    
    private void ListAuctions()
    {
        SocketOutput.println("C");
    }
    
    private void RegisterAuction(BufferedReader bf) throws IOException
    {
        System.out.println("Digite uma descrição do que pretende leiloar");
        SocketOutput.println("V|"+Menu.readString(bf));
    }
    
    private void BidAuction(BufferedReader bf) throws IOException
    {
        System.out.println("Digite o número do leilão");
        String AuctionNumber=Menu.readPosLong(bf);
        System.out.println("Digite o montante que quer licitar");
        String BidAmount=Menu.readPosFloat(bf);
        SocketOutput.println("C|"+AuctionNumber+"|"+BidAmount);
    }
    
    private void EndAuction(BufferedReader bf) throws IOException
    {
        System.out.println("Digite o número do leilão");
        String AuctionNumber=Menu.readPosLong(bf);
        SocketOutput.println("V|"+AuctionNumber);
    }
    
}
