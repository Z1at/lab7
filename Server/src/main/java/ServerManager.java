import NikandrovLab5.commands.*;
import NikandrovLab5.data.*;
import NikandrovLab5.utility.Collection;
import NikandrovLab5.utility.Database;
import NikandrovLab5.utility.TextFormatting;
import NikandrovLab5.utility.Transformation;
import src.ClientMessage;
import src.ServerMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.Vector;

public class ServerManager {
    ServerSender serverSender;
    ServerReceiver serverReceiver;
    String environmentVariable;
    TreeMap<String, byte[]> users = new TreeMap<>();
    Database database;


    public ServerManager(ServerReceiver serverReceiver, ServerSender serverSender, String environmentVariable, Database database){
        this.serverReceiver = serverReceiver;
        this.serverSender = serverSender;
        this.environmentVariable = environmentVariable;
        this.database = database;
    }

    public void run(Collection collection) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, SQLException {
        ServerMessage answer = new ServerMessage("The command was executed" + '\n');
        ByteBuffer byteBuffer = serverReceiver.receive(serverSender);
        ClientMessage clientMessage = (ClientMessage) Transformation.Deserialization(byteBuffer);
        if (clientMessage.command != null) {
            if (clientMessage.command.equals("insert")) {
                if (clientMessage.arg.contains(",")) {
                    answer.setMessage("There can be no commas in the key" + '\n');
                } else {

                    City city = (City) clientMessage.obj;
                    city.setId(collection.id++);
                    city.setCreationDate();

                    String query = "INSERT INTO 'users' ('key', 'name', 'coordinates of x', 'coordinates of y'," +
                            "'creation date', 'area', 'population', 'meters', 'climate', 'government', 'standard of living'," +
                            "'name of governor', 'height of governor', 'birthday of governor', 'creator') VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement statement = database.conn.prepareStatement(query);
                    statement.setString(1, clientMessage.arg); statement.setString(2, city.getName());
                    statement.setFloat(3, city.getCoordinates().getX()); statement.setDouble(4, city.getCoordinates().getY());
                    statement.setString(5, city.getCreationDate().toString()); statement.setDouble(6, city.getArea());
                    statement.setLong(7, city.getPopulation()); statement.setInt(8, city.getMetersAboveSeaLevel());
                    statement.setString(9, city.getClimate().toString()); statement.setString(10, city.getGovernment().toString());
                    statement.setString(11, city.getStandardOfLiving().toString()); statement.setString(12, city.getGovernor().getName());
                    statement.setDouble(13, city.getGovernor().getHeight()); statement.setString(14, city.getGovernor().getBirthday().toString());
                    statement.setString(15, clientMessage.login);
                    statement.execute();

                    collection.collection.put(clientMessage.arg, city);
                    if(collection.creators.containsKey(clientMessage.login)) {
                        collection.creators.get(clientMessage.login).add(city);
                    }
                    else {
                        collection.creators.put(clientMessage.login, new Vector<>());
                        collection.creators.get(clientMessage.login).add(city);
                    }
                    answer.setMessage("The object was successfully added" + '\n');
                }
            } else {
                ResultSet resSet = database.statmt.executeQuery("SELECT * FROM users");
                while(resSet.next()){
                    if(resSet.getInt("id") == clientMessage.id){
                        String query = "UPDATE 'users' SET ('name') VALUES(?)";
                        PreparedStatement statement = database.conn.prepareStatement(query);
                        statement.execute();
                    }
                }


                String key = null;
                for (String now : collection.collection.keySet()) {
                    if (collection.collection.get(now).getId() == clientMessage.id) {
                        key = now;
                        break;
                    }
                }
                if (key == null) {
                    answer.setMessage("There is no item with this id in the collection" + '\n');
                }
                boolean flag = false;
                for(City city :collection.creators.get(clientMessage.login)){
                    if(city.equals(collection.collection.get(key))){
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    answer.setMessage("You did not create this object and cannot change it");
                }
                else {
                    switch (clientMessage.arg) {
                        case "name" -> collection.collection.get(key).setName((String) clientMessage.obj);
                        case "coordinates" -> collection.collection.get(key).setCoordinates((Coordinates) clientMessage.obj);
                        case "area" -> collection.collection.get(key).setArea((Double) clientMessage.obj);
                        case "population" -> collection.collection.get(key).setPopulation((Long) clientMessage.obj);
                        case "metersabvovesealevel" -> collection.collection.get(key).setMetersAboveSeaLevel((Integer) clientMessage.obj);
                        case "climate" -> collection.collection.get(key).setClimate((Climate) clientMessage.obj);
                        case "government" -> collection.collection.get(key).setGovernment((Government) clientMessage.obj);
                        case "standardofliving" -> collection.collection.get(key).setStandardOfLiving((StandardOfLiving) clientMessage.obj);
                        case "governor" -> collection.collection.get(key).setGovernor((Human) clientMessage.obj);
                    }
                    answer.setMessage("The element has been successfully replaced" + '\n');
                }
            }
        }
        else if(clientMessage.commands[0].equals("register")){
            if(users.containsKey(clientMessage.login)){
                answer.setMessage("This login already exists");
            }
            else{
                answer.setMessage("You have successfully registered");
                users.put(clientMessage.login, MessageDigest.getInstance("SHA-1").digest(clientMessage.password.getBytes()));
            }
        }
        else if(clientMessage.commands[0].equals("check")){
            answer.setMessage("Invalid username or password");
            if(users.containsKey(clientMessage.login)){
                if(Arrays.equals(users.get(clientMessage.login), MessageDigest.getInstance("SHA-1").digest(clientMessage.password.getBytes()))) {
                    answer.setMessage("You have successfully logged into your account");
                }
            }
        }
        else{
            Operations operations = new Operations();
            answer.setMessage("");
            operations.run(clientMessage.commands, collection, environmentVariable, answer, operations, clientMessage.login, clientMessage.password, database);
            if(answer.message.equals("")){
                answer.setMessage("The command was executed" + '\n');
            }
        }

        for(String now : collection.creators.keySet()){
            System.out.println(now);
            for(City city : collection.creators.get(now)){
                System.out.println(city.toString());
            }
        }
        ByteBuffer buffer = Transformation.Serialization(answer);
        serverSender.send(buffer);
    }
}
