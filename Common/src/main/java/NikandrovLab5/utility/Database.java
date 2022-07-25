package NikandrovLab5.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class Database {
    public Connection conn;
    public Statement statmt;

    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    public void connection() throws ClassNotFoundException, SQLException
    {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:TstDataBase");

        System.out.println("База Подключена!");
    }

    // --------Создание таблицы--------
    public void createDB() throws ClassNotFoundException, SQLException
    {
        statmt = conn.createStatement();
        statmt.execute("CREATE TABLE if not exists 'users' ('key' text, 'id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text, " +
                "'coordinates of x' INTEGER, 'coordinates of y' INTEGER, 'creation date' text, 'area' INTEGER, 'population' INTEGER, 'meters' INTEGER, " +
                "'climate' text, 'government' text, 'standard of living' text, 'name of governor' text, 'height of governor' REAL, 'birthday of governor' text, 'creator' text);");

        System.out.println("Таблица создана или уже существует.");
    }
}
