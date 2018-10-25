package cn.edu.swufe.feifeimusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private DBHelper dbHelper;
    private String TBNAME;
    public DBManager(Context context){
        dbHelper =new DBHelper(context);
        TBNAME=DBHelper.TB_NAME;
    }
    public void add(MusicItem item){
        SQLiteDatabase db =dbHelper.getWritableDatabase();
        ContentValues values= new ContentValues();
        Song song=item.getSong();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(song);
            objectOutputStream.flush();
            objectOutputStream.close();
            arrayOutputStream.close();
            byte data[] = arrayOutputStream.toByteArray();
            values.put("music",data);
            values.put("position",item.getPosition());
            db.insert(TBNAME,null,values);
            db.close();
        }catch (Exception e){
            Log.i("error\t", "add: "+e);
        }


    }
    public void addAll(List<MusicItem> list){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        for(MusicItem item:list){
            ContentValues values=new ContentValues();
            Song song=item.getSong();
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
                objectOutputStream.writeObject(song);
                objectOutputStream.flush();
                objectOutputStream.close();
                arrayOutputStream.close();
                byte data[] = arrayOutputStream.toByteArray();
                values.put("music",data);
                values.put("position",item.getPosition());
                db.insert(TBNAME,null,values);
                db.close();
            }catch (Exception e){
                Log.i("error\t", "add: "+e);
            }
        }
        db.close();
    }
    public void deleteAll(){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.delete(TBNAME,null,null);
        db.close();
    }
    public void delete(int id){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.delete(TBNAME,"id=?",new String[]{String.valueOf(id)});
        db.close();
    }
    public void update(MusicItem item){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values= new ContentValues();
        Song song=item.getSong();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(song);
            objectOutputStream.flush();
            objectOutputStream.close();
            arrayOutputStream.close();
            byte data[] = arrayOutputStream.toByteArray();
            values.put("music", data);
            values.put("position", item.getPosition());
            db.update(TBNAME, values, "ID=?", new String[]{String.valueOf(0)});
            db.close();
        }catch (Exception e){
            Log.i("erro\t", "update: "+e);
        }
    }
    public List<MusicItem> listAll(){
        List<MusicItem> rateList =null;
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor=db.query(TBNAME,null,null,null,null,null,null);
        if(cursor!=null){
            rateList =new ArrayList<MusicItem>();
            while(cursor.moveToNext()){
                byte data[] = cursor.getBlob(cursor.getColumnIndex("SONG"));
                ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
                try {
                    ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                    Song song=(Song) inputStream.readObject();
                    MusicItem item = new MusicItem();
                    item.setSong(song);
                    item.setPosition(cursor.getInt(cursor.getColumnIndex("POSITION")));
                    inputStream.close();
                    arrayInputStream.close();
                    rateList.add(item);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        db.close();;
        return rateList;
    }
//    public MusicItem findById(int id){
//        SQLiteDatabase db= dbHelper.getReadableDatabase();
//        Cursor cursor= db.query(TBNAME,null,"ID=?",new String[]{String.valueOf(id)},null,null,null);
//        MusicItem MusicItem=null;
//        if(cursor!=null && cursor.moveToFirst()){
//            MusicItem=new MusicItem();
//            MusicItem.setId(cursor.getInt(cursor.getColumnIndex("ID")));
//            MusicItem.setCurName(cursor.getString(cursor.getColumnIndex("CURNAME")));
//            MusicItem.setCurRate(cursor.getString(cursor.getColumnIndex("CURRATE")));
//        }
//        cursor.close();
//        db.close();
//        return MusicItem;
//    }

}
