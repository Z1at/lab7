package NikandrovLab5.commands;

import NikandrovLab5.data.*;
import NikandrovLab5.utility.Collection;
import NikandrovLab5.utility.Database;
import NikandrovLab5.utility.FieldReceiverForFile;

import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * Class for executing two commands from a file
 */
public class FileOutput {
    public static void insert(Collection collection, String key, FileReader file, String login, String password, Database database) throws IOException, SQLException {
        String name = FieldReceiverForFile.getName(file);
        Coordinates coordinates = FieldReceiverForFile.getCoordinates(file);
        double area = FieldReceiverForFile.getArea(file);
        Long population = FieldReceiverForFile.getPopulation(file);
        Integer metersAboveSeaLevel = FieldReceiverForFile.getMetersAboveSeaLevel(file);
        Climate climate = FieldReceiverForFile.getClimate(file);
        Government government = FieldReceiverForFile.getGovernment(file);
        StandardOfLiving standardOfLiving = FieldReceiverForFile.getStandardOfLiving(file);
        Human governor = FieldReceiverForFile.getGovernor(file);

        if (name != null & coordinates.getY() != (long) -1 & area != (double) -1 & population != (long) -1 &
                metersAboveSeaLevel != null & climate != null & government != null & standardOfLiving != null & governor != null) {
            City city = new City(name, coordinates, area, population, metersAboveSeaLevel, climate, government, standardOfLiving, governor);
            city.setId(collection.id++);
            city.setCreationDate();

            String query = "INSERT INTO 'users' ('key', 'name', 'coordinates of x', 'coordinates of y'," +
                    "'creation date', 'area', 'population', 'meters', 'climate', 'government', 'standard of living'," +
                    "'name of governor', 'height of governor', 'birthday of governor', 'creator') VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement statement = database.conn.prepareStatement(query);
            statement.setString(1, key); statement.setString(2, city.getName());
            statement.setFloat(3, city.getCoordinates().getX()); statement.setDouble(4, city.getCoordinates().getY());
            statement.setString(5, city.getCreationDate().toString()); statement.setDouble(6, city.getArea());
            statement.setLong(7, city.getPopulation()); statement.setInt(8, city.getMetersAboveSeaLevel());
            statement.setString(9, city.getClimate().toString()); statement.setString(10, city.getGovernment().toString());
            statement.setString(11, city.getStandardOfLiving().toString()); statement.setString(12, city.getGovernor().getName());
            statement.setDouble(13, city.getGovernor().getHeight()); statement.setString(14, city.getGovernor().getBirthday().toString());
            statement.setString(15, login);
            statement.execute();

            collection.collection.put(key, city);
            if(collection.creators.containsKey(login)) {
                collection.creators.get(login).add(city);
            }
            else {
                collection.creators.put(login, new Vector<>());
                collection.creators.get(login).add(city);
            }
        }
    }

    public static void updateId(Collection collection, int id, FileReader file, String login, String password, Database database) throws IOException {
        String copyKey = "";
        for (String key : collection.collection.keySet()) {
            if (collection.collection.get(key).getId() == id) {
                copyKey = key;
                break;
            }
        }


        if (!copyKey.equals("")) {
            boolean flag = false;
            for(City city :collection.creators.get(login)){
                if(city.equals(collection.collection.get(copyKey))){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return;
            }



            String[] string = getLine(file).toLowerCase().trim().split(" ");
            StringBuilder concatenation = new StringBuilder();
            for (String temporary : string) {
                concatenation.append(temporary);
            }
            switch (concatenation.toString()) {
                case "name" -> {
                    String name = FieldReceiverForFile.getName(file);
                    if (name != null) {
                        collection.collection.get(copyKey).setName(name);
                    }
                }
                case "coordinates" -> {
                    Coordinates coordinates = FieldReceiverForFile.getCoordinates(file);
                    if (coordinates.getY() != -1) {
                        collection.collection.get(copyKey).setCoordinates(coordinates);
                    }
                }
                case "area" -> {
                    double area = FieldReceiverForFile.getArea(file);
                    if (area > 0) {
                        collection.collection.get(copyKey).setArea(area);
                    }
                }
                case "population" -> {
                    Long population = FieldReceiverForFile.getPopulation(file);
                    if (population > 0) {
                        collection.collection.get(copyKey).setPopulation(population);
                    }
                }
                case "metersabvovesealevel" -> {
                    Integer metersAboveSeaLevel = FieldReceiverForFile.getMetersAboveSeaLevel(file);
                    if (metersAboveSeaLevel != null) {
                        collection.collection.get(copyKey).setMetersAboveSeaLevel(metersAboveSeaLevel);
                    }
                }
                case "climate" -> {
                    Climate climate = FieldReceiverForFile.getClimate(file);
                    if (climate != null) {
                        collection.collection.get(copyKey).setClimate(climate);
                    }
                }
                case "government" -> {
                    Government government = FieldReceiverForFile.getGovernment(file);
                    if (government != null) {
                        collection.collection.get(copyKey).setGovernment(government);
                    }
                }
                case "standardofliving" -> {
                    StandardOfLiving standardOfLiving = FieldReceiverForFile.getStandardOfLiving(file);
                    if (standardOfLiving != null) {
                        collection.collection.get(copyKey).setStandardOfLiving(standardOfLiving);
                    }
                }
                case "governor" -> {
                    Human governor = FieldReceiverForFile.getGovernor(file);
                    if (governor != null) {
                        collection.collection.get(copyKey).setGovernor(governor);
                    }
                }
            }
        }
    }

    public static String getLine(FileReader file) throws IOException {
        String string = "";
        while (file.ready()) {
            char c = (char) file.read();
            if (c == '\n') {
                break;
            }
            string += c;
        }
        return string;
    }
}
