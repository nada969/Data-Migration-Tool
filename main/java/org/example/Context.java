package org.example;

import java.sql.SQLException;

public class Context {
    private javaSQL javaSQL;

    public void setJavaSQL(javaSQL javaSQL){
        this.javaSQL = javaSQL;
    }

    public void execute(String sql,int i,Object value) throws SQLException {
        this.javaSQL.type(sql,i,value);
    }


}
