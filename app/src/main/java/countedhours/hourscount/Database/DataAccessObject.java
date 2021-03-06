package countedhours.hourscount.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DataAccessObject {

    @Insert
    public void insertValues(Sheets sheets);

    @Query("select * from Sheets")
    public List<Sheets> retrieveSheetsData();
}
