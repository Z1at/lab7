import NikandrovLab5.commands.Save;
import NikandrovLab5.data.City;
import NikandrovLab5.data.Coordinates;
import NikandrovLab5.data.Human;
import NikandrovLab5.utility.Collection;
import NikandrovLab5.utility.Database;
import NikandrovLab5.utility.TextFormatting;
import NikandrovLab5.utility.Transformation;
import src.ClientMessage;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Server {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        DatagramChannel serverChannel = DatagramChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress("localhost", 777));
        Collection collection = new Collection();
        ServerReceiver serverReceiver = new ServerReceiver(serverChannel);
        ServerSender serverSender = new ServerSender(serverChannel);
//        String environmentVariable = System.getenv("Lab6");
        String environmentVariable = args[0];

        //Создаём базу данных и читаем из неё данные
        Database database = new Database();
        database.connection();
        database.createDB();
        ResultSet resSet = database.statmt.executeQuery("SELECT * FROM users");
        while(resSet.next()){
            String key = resSet.getString("key");
            City city = new City();
            city.setId(resSet.getInt("id")); city.setName(resSet.getString("name"));
            city.setCoordinates(new Coordinates(resSet.getFloat("coordinates of x"), resSet.getLong("coordinates of y")));
            city.setCreationDate(resSet.getString("creation date")); city.setArea(resSet.getDouble("area"));
            city.setPopulation(resSet.getLong("population")); city.setMetersAboveSeaLevel(resSet.getInt("meters"));
            city.setClimate(resSet.getString("climate")); city.setGovernment(resSet.getString("government"));
            city.setStandardOfLiving(resSet.getString("standard of living"));
            city.setGovernor(new Human(resSet.getString("name of governor"), resSet.getDouble("height of governor"), resSet.getString("birthday of governor")));
            city.setCreator(resSet.getString("creator"));

            collection.collection.put(key, city);
        }

        while(true){
            Scanner scanner = new Scanner(System.in);
            if(environmentVariable == null){
                System.out.println("The environment variable has not been set, enter the path to the file that can be read and written to:");
                environmentVariable = scanner.nextLine();
            }
            File check = new File(environmentVariable);
            try {
                check.createNewFile();
                break;
            }
            catch(Exception ignored){

            }
            environmentVariable = null;
        }
        environmentVariable = environmentVariable.toLowerCase();
        System.out.println("The server has started working");
        ServerManager serverManager = new ServerManager(serverReceiver, serverSender, environmentVariable, database);

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_READ);
        new Thread(() -> {
            while(true) {
                try {
                    selector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while(it.hasNext()){
                    SelectionKey key = it.next();
                    it.remove();
                    if(key.isReadable()) {
                        try {
                            serverManager.run(collection);
                        }
                        catch(Exception exception){
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }).start();

        String finalEnvironmentVariable = environmentVariable;
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while(true){
                if(scanner.hasNext()){
                    String command = scanner.nextLine();
                    if(command.equals("exit")){
//                        Save save = new Save();
//                        save.save(finalEnvironmentVariable, collection);
                        System.out.println(TextFormatting.getYellowText("The program is over, I hope you enjoyed it"));
                        System.exit(0);
                    }
//                    else if(command.equals("save")){
//                        Save save = new Save();
//                        System.out.println(TextFormatting.getYellowText(save.save(finalEnvironmentVariable, collection)));
//                    }
                    else{
                        System.out.println(TextFormatting.getRedText("Unknown command"));
                    }
                }
            }
        }).start();
    }
}
