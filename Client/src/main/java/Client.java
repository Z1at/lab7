import NikandrovLab5.utility.TextFormatting;
import NikandrovLab5.utility.Transformation;
import org.w3c.dom.Text;
import src.ClientMessage;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        DatagramChannel clientChannel = DatagramChannel.open();
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", Integer.parseInt(args[0]));
        ClientSender clientSender = new ClientSender(clientChannel, serverAddress);
        ClientReceiver clientReceiver = new ClientReceiver(clientChannel);

        String[] user = Authorization.authorization(clientChannel, serverAddress);
        ClientManager clientManager = new ClientManager(clientReceiver, clientSender, user);

        try{
            while(true){
                System.out.println(TextFormatting.getGreenText("Enter the command:"));
                String command = scanner.nextLine().strip();
                clientManager.run(command);
                if(command.equals("exit")){
                    break;
                }
            }
        }
        catch (Exception exception){
            System.out.println(TextFormatting.getYellowText(exception.getMessage()));
        }
        finally {
            System.out.println(TextFormatting.getYellowText("The program is over, I hope you enjoyed it"));
        }

        clientChannel.close();
    }
}
