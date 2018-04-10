package com.example.alex.androidlabs;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class
ChatWindow extends AppCompatActivity {

    ListView listView;
    EditText chatText;
    Button chatButton;
    ChatDatabaseHelper helper;
    SQLiteDatabase database;
    protected static final String ACTIVITY_NAME ="ChatWindow";
    Cursor cursor;
    //private boolean isFrameLoaded;
    ChatAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getBaseContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            setContentView(R.layout.activity_chat_window_tablet);
        else
            setContentView(R.layout.activity_chat_window);

        listView = (ListView)findViewById(R.id.listViewChat);
        chatText = (EditText)findViewById(R.id.textChat);
        chatButton = (Button)findViewById(R.id.sendButton);

       // isFrameLoaded = findViewById(R.id.message_detail_container) != null;

        helper = new ChatDatabaseHelper(this);
        database = helper.getWritableDatabase();

        messageAdapter = new ChatAdapter( this );


        String[] allColumns = {ChatDatabaseHelper.KEY_ID, ChatDatabaseHelper.KEY_MESSAGE};
        cursor = database.query(helper.TABLE_NAME, allColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while(cursor.moveToNext()){
            String message = cursor.getString( cursor.getColumnIndex(helper.KEY_MESSAGE) );
            long id = cursor.getLong(cursor.getColumnIndex(ChatDatabaseHelper.KEY_ID));
            Log.i(ACTIVITY_NAME, "SQL MESSAGE: " + cursor.getString( cursor.getColumnIndex(helper.KEY_MESSAGE) ) );

        }

        Log.i(ACTIVITY_NAME, "Cursors column count =" + cursor.getColumnCount());
        for (int x = 0; x < cursor.getColumnCount(); x++) {
            Log.i("Cursor column name", cursor.getColumnName(x));
        }//end for

        //in this case, “this” is the ChatWindow, which is-A Context object

        listView.setAdapter (messageAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                Log.d("ListView","onItemClick: " + position + " " + id);

                Bundle bundle = new Bundle();
                bundle.putLong("ID", id );//id is the database ID of selected item
                String clickedMessage = (String) (listView.getItemAtPosition(position));// get the message of the clicked item
                bundle.putString("Message",clickedMessage); //

                //lab7-step 2, if is a tablet, insert fragment into FrameLayout, pass data
                //getBaseContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                if(getBaseContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    bundle.putBoolean("isTablet",true);
                    MessageFragment frag = new MessageFragment();
                    frag.setArguments(bundle);
                    getFragmentManager().beginTransaction().add(R.id.message_detail_container,frag).commit();
                }
                //lab7-step 3 if is a phone, transition to empty Activity that has FrameLayout
                else
                {
                    Intent intent = new Intent(ChatWindow.this, MessageDetails.class);
                    intent.putExtras(bundle); //pass the clicked item ID and message to next activity
                    startActivityForResult(intent,3); //go to MessageDetais activity and view message details
                }
            }
        });



        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(ChatDatabaseHelper.KEY_MESSAGE, chatText.getText().toString());
                database.insert(ChatDatabaseHelper.TABLE_NAME, "null", contentValues);
                chatText.setText("");
                String[] allColumns = {ChatDatabaseHelper.KEY_ID, ChatDatabaseHelper.KEY_MESSAGE};
                cursor = database.query(helper.TABLE_NAME, allColumns, null, null, null, null, null);
                cursor.moveToFirst();
                messageAdapter.notifyDataSetChanged(); //this restarts the process of getCount() & getView()

            }
        });
    }

    private class ChatAdapter extends ArrayAdapter<String> {
        public ChatAdapter(Context ctx) {
            super(ctx, 0);
        }

        public int getCount() {
            return cursor.getCount();
        }

        public long getItemId(int position){
            //return Long.getLong(list.get(position));
            //return cursor.moveToPosition(position);
            if(cursor.moveToPosition(position))
                return  cursor.getLong(0);
            else
                return -1;
        }

        public String getItem(int position) {
            if(cursor.moveToPosition(position))
                return  cursor.getString(1);
            else
                return "";
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

        public long getId(int position){

            return position;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            int id = extras.getInt("id");

            database.delete(ChatDatabaseHelper.TABLE_NAME, "_id=?",
                    new String[]{Integer.toString(id)});
            String[] allColumns = {ChatDatabaseHelper.KEY_ID, ChatDatabaseHelper.KEY_MESSAGE};
            cursor = database.query(helper.TABLE_NAME, allColumns, null, null, null, null, null);
            cursor.moveToFirst();

            messageAdapter.notifyDataSetChanged();
        }
    }

    //lab7-delete clicked item from the listview
    public void deleteMessage(int id) {
//       int del= db.delete(ChatDatabaseHelper.TABLE_NAME, "_id=?",new String[]{Integer.toString(id)});
        int del= database.delete(ChatDatabaseHelper.TABLE_NAME, "_id=?",new String[]{Integer.toString(id)});
        String[] allColumns = {ChatDatabaseHelper.KEY_ID, ChatDatabaseHelper.KEY_MESSAGE};
        cursor = database.query(helper.TABLE_NAME, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        messageAdapter.notifyDataSetChanged();

        MessageFragment mf = (MessageFragment)getFragmentManager().findFragmentById(R.id.message_detail_container);
        getFragmentManager().beginTransaction().remove(mf).commit();

    }

    @Override
    protected void onDestroy() {
        Log.i(ACTIVITY_NAME, "In onDestroy()");
        super.onDestroy();
        cursor.close();
        database.close();
    }



    public class DbInfo{
        public long id;
        public String message;

        DbInfo(long id, String message){
            this.id=id;
            this.message=message;
        }

        public long getId(){
            return id;
        }
    }
}
