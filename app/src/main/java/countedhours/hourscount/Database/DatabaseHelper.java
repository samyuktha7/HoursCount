package countedhours.hourscount.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/*
Room database is implemented to store the sheets data. Replaced with Sqlite Database. (much simpler)

- Adds dependencies in gradle file

1. Entity : table name. - Must have a primary key and Column Info (other columns) - getter and setter methods
2. Data Access Objects: defines all the operations which can be performed on the tables. can use one DAO for multiple tables
3. DatabaseHelper class - which creates the database and declares the data access object. - extends Room Database

- should create database helper object which specifies database name, version and builds
many tables ----- one Dao ----- one DatabaseHelper
 */


@Database(entities = {Sheets.class}, version = 1)
public abstract class DatabaseHelper extends RoomDatabase {

    public abstract DataAccessObject dao();

}
