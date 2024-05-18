package com.example.edge_health.Databases;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import com.example.edge_health.Token;

@Dao
public interface TokenDao {
    @Upsert
    void insert(Token token);

    @Delete
    void delete(Token token);

    @Query("DELETE FROM Token")
    void deleteAll();

    @Query("SELECT * FROM Token")
    Token[] getAll();

    @Query("SELECT * FROM Token WHERE id = :id")
    Token getById(long id);
}
