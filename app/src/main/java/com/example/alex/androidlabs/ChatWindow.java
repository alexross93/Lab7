package com.example.alex.androidlabs;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class
ChatWindow extends AppCompatActivity {

    ListView listView;
    EditText chatText;
    Button chatButton;
    final ArrayList<String> list = new ArrayList<>();
    ChatDatabaseHelper helper;
    SQLiteDatabase database;
    protected static final String ACTIVITY_NAME ="ChatWindow";
    Cursor cursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        listView = (ListView)findViewById(R.id.listViewChat);
        chatText = (EditText)findViewById(R.id.textChat);
        chatButton = (Button)findViewById(R.id.sendButton);

        helper = new ChatDatabaseHelper(this);
        database = helper.getWritableDatabase();


        String[] allColumns = {ChatDatabaseHelper.KEY_ID, ChatDatabaseHelper.KEY_MESSAGE};
        cursor = database.query(helper.TABLE_NAME, allColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while(cursor.moveToNext()){
            list.add( cursor.getString( cursor.getColumnIndex(helper.KEY_MESSAGE) ) );
            Log.i(ACTIVITY_NAME, "SQL MESSAGE: " + cursor.getString( cursor.getColumnIndex(helper.KEY_MESSAGE) ) );
        }

        Log.i(ACTIVITY_NAME, "Cursors column count =" + cursor.getColumnCount());
        for (int x = 0; x < cursor.getColumnCount(); x++) {
            Log.i("Cursor column name", cursor.getColumnName(x));
        }//end for

        //in this case, “this” is the ChatWindow, which is-A Context object
        final ChatAdapter messageAdapter =new ChatAdapter( this );
        listView.setAdapter (messageAdapter);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                list.add(list.size(),chatText.getText().toString());
                ContentValues contentValues = new ContentValues();
                contentValues.put(ChatDatabaseHelper.KEY_MESSAGE, chatText.getText().toString());
                database.insert(ChatDatabaseHelper.TABLE_NAME, "null", contentValues);
                chatText.setText("");

                messageAdapter.notifyDataSetChanged(); //this restarts the process of getCount() & getView()

            }
        });
    }

    private class ChatAdapter extends ArrayAdapter<String> {
        public ChatAdapter(Context ctx) {
            super(ctx, 0);
        }

        public int getCount() {
            return list.size();
        }

        public String getItem(int position) {
            return list.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent){

            LayoutInflater inflater = ChatWindow.this.getLayoutInflater();
            View result = null ;
            if(position%2 == 0)
                result = inflater.inflate(R.layout.chat_row_incoming, null);
            else
                result = inflater.inflate(R.layout.chat_row_outgoing, null);

            TextView message = (TextView) result.findViewById(R.id.message_text);
            message.setText(   getItem(position)  ); // get the string at position
            return result;

        }

        long getId(int position){

            return position;
        }

    }

    @Override
    protected void onDestroy() {
        Log.i(ACTIVITY_NAME, "In onDestroy()");
        super.onDestroy();
        cursor.close();
        database.close();
    }
}
