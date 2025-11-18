package org.example;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQL {
    public Connection conn;
    public void connect(String url,String user,String password) {
//        Postgresql DB name (destination)
//        String url = "jdbc:postgresql://localhost:5432/postgres";
//        String user = "postgres";                                    // PostgreSQL username
//        String password = "493075273";                               // PostgreSQL password
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to PostgreSQL successfully!");
        } catch (Exception e) {
            System.out.println("exceptionnnnnnnnnn:" + e.toString());
        }

    }

    public void close()  {
        try {
            this.conn.close();
            System.out.println("Close PostgreSQL successfully!");
        } catch (Exception e){
            System.out.println("Exection in closing: "+e.toString());
        }

    }

}
