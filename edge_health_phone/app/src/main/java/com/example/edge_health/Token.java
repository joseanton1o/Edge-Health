package com.example.edge_health;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Token {
    @PrimaryKey
    private long id;
    @ColumnInfo(name = "token")
    private String token;

    public Token(String token, long id) {
        this.token = token;
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public long getId() {
        return id;
    }
}
