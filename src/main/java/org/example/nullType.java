package org.example;

import java.sql.PreparedStatement;
import java.sql.Types;

public class nullType implements javaSQL{

    @Override
    public void type(int i,Object value) {
        System.out.println(i+": Null Value");
    }
}
