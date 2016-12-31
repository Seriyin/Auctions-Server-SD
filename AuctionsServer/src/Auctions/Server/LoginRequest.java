/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

import java.net.Socket;

/**
 *
 * @author Andre
 */
public class LoginRequest 
{
    private final boolean isLogin;
    private final String Username;
    private final String Password;
    private final Socket RequestSocket;

    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return Password;
    }

    public Socket getRequestSocket() {
        return RequestSocket;
    }
    
    public LoginRequest(boolean isLogin, 
                        String Username,
                        String Password,
                        Socket RequestSocket) 
    {
        this.isLogin=isLogin;
        this.Username=Username;
        this.Password=Password;
        this.RequestSocket=RequestSocket;
    }
    
    public boolean isLogin() 
    {
        return isLogin;
    }
}
