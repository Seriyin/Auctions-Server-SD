package Auctions.UI;

import java.io.BufferedReader;
import java.io.IOException;
/**
 * Interface Funcional que apenas corre o método correspondente 
 * com um BufferedReader que recebe.
 * 
 * Pode atirar uma exceção IO caso o terminal exploda.
 * 
 * @author André Sousa Diogo, António Pedro Silva, Luís Gonçalo Epifânio Pereira
 * @version 9/6/2016
 */
@FunctionalInterface
public interface InputProcedure extends MenuFunctions
{
    public void accept(BufferedReader t) throws IOException;
}
