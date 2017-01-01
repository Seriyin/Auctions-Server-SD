/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auctions.Server;

/**
 * InactiveAuctionException is a RuntimeException. Extra with it as it does
 * not need to be explicitly thrown or caught and can propagate and blow up
 * the server.
 * @author André Diogo, Gonçalo Pereira, António Silva
 */
class InactiveAuctionException extends RuntimeException {
    public String message() 
    {
        return "Leilão já terminado";
    }
}
