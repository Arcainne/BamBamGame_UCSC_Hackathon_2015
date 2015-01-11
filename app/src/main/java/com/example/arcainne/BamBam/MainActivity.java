package com.example.arcainne.BamBam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private static final int
            KEY_GESTURE = 0,
            JAZZ_HANDS = 1,  // Jazz Hands
            POKE = 2,  //Poke
            RAISE_THE_ROOF = 3,  //Raise the Roof
            RUNNING= 4, //running
            DOWN=5, //Down Button
            UP= 6, //Up button
            SELECT= 7,
            KEY_SEND_ROLE= 8,
            KEY_SEND_PHASE=9,
            KEY_SCORE_UPDATE= 10,
            WAITING_ROOM_SCREEN= 11,
            GAME_PLAY_SCREEN=12,
            FINAL_SCREEN=13;

    //This is the connection to our particular phone app (determined by UUID given in cloudpebble)
    private UUID Pebble_UUID = UUID.fromString("7c02f3fb-ff81-4893-aa1c-f741b2e7c3ff");
    private PebbleKit.PebbleDataReceiver pebbleDataReceiverReceiver; //this is our data receiver

    private PebbleDictionary[] lastMessage= new PebbleDictionary[3];
    private static int transactionID=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to handle button click
    public void loadJoinScreen(View view) {
        // Do stuff in response to button press
        Intent intent = new Intent(this, GamePlayActivity.class);
        startActivity(intent);
    }

    public void loadHostScreen(View view) {
        // Do stuff in response to button press
        Intent intent = new Intent(this, HostActivity.class);
        startActivity(intent);
    }
}
