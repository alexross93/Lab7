package com.example.alex.androidlabs;

import android.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MessageDetails extends AppCompatActivity {
    Long id;
    //Immediately create the fragment and insert it in the framelayout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        //Step 3, create fragment onCreation, pass data from Intent Extras to FragmentTransction
        MessageFragment frag = new MessageFragment();
        Bundle bundle  =this.getIntent().getExtras();
        id =bundle.getLong("ID");
        frag.setArguments( bundle );
        getFragmentManager().beginTransaction().add(R.id.emptyframelayout, frag).commit();

    }
}
