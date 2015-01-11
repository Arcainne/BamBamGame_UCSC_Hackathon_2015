package com.example.arcainne.BamBam;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class GamePlayActivity extends ActionBarActivity {

    // View Vars
    private TextView tvInstructions;
    private TextView tvScore;

    /************ INITALIZATION of GLOBAL VARIABLES FOR WATCH INTERACTIONS *******/
    //This needs to be used for communication
    //This is a list of commands from watch to client
    private static final int
            KEY_GESTURE = 0,
            GESTURE_1 = 1,
            GESTURE_2 = 2,
            GESTURE_3 = 3;

    //This is a list of commands from client to watch
    private static final int
            KEY_SEND_ROLE= 4,       //Key, use a string as data
            KEY_SEND_PHASE=5,       //Key, use an enum below as data
            KEY_SCORE_UPDATE= 6,    //Key, use an int as data
            WAITING_ROOM_SCREEN= 7,
            GAME_PLAY_SCREEN=8,
            FINAL_SCREEN=9;

    //This is the connection to our particular phone app (determined by UUID given in cloudpebble)
    private UUID Pebble_UUID = UUID.fromString("7c02f3fb-ff81-4893-aa1c-f741b2e7c3ff");
    private PebbleKit.PebbleDataReceiver pebbleDataReceiverReceiver; //this is our data receiver

    private PebbleDictionary[] lastMessage= new PebbleDictionary[3];
    private static int transactionID=0;

    /**********END OF VARIABLES FOR PEBBLE INTERACTION ******/

    /********** VARS FOR GAMEPLAY ************/
    private int playerCount;
    private int score;

    // Create array of player roles
    private String roles[]= {
            "GnomesInRome",
            "BamBamNotBatman",
            "SeanAndSamson",
            "RobABaby",
            "Sojalicious",
            "OrcsALot",
            "Coxpiece",
            "Peterpanopolis",
            "Maximillion"
    };

    // Create array of possible tasks to perform
    private String tasks[] = {
            "Jazz hands",
            "Poke",
            "Raise the roof",
            "Top button",
            "Middle button",
            "Bottom button"
    };

    // Active instructions
    //Instruction activeInstructions[];
    private List activeInstructions;

    /********** END VARS FOR GAMEPLAY **********/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        // Get TextView instructions object from activity_game_play.xml
        tvInstructions = (TextView) findViewById(R.id.instruction_id);
        tvInstructions.setText("No instruction yet!");

        //Place in OnCreate Code to start up Pebble
        startWatchApp();

        // Initialize vars
        playerCount = 0;
        score = 0;

        // Make random number of players for testing (2 - 4)
        Random r = new Random();
        playerCount = r.nextInt(3) + 2;

        // Create current active instructions list
        activeInstructions = new ArrayList();
        for (int i = 0; i < playerCount; i++) {
            // Generate and add new random instruction to active instructions list
            Instruction instr = new Instruction(chooseRandomRole(), chooseRandomTask());
            activeInstructions.add(instr);
        }

        // Set score text on screen
        tvScore = (TextView)findViewById(R.id.score_id);
        tvScore.setText("SCORE: " + score);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_play, menu);
        return true;
    }
    */

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

    @Override
    protected void onResume() {
        super.onResume();
        startWatchApp();

        //TODO: Differenciate players/phones/clients to send different instructions
        // Get random instruction for activeInstructions and send to phone
        Random r = new Random();
        int index = r.nextInt(activeInstructions.size());
        String instrTask = ((Instruction)activeInstructions.get(index)).task;
        String instrPerformer = ((Instruction)activeInstructions.get(index)).performer;
        tvInstructions.setText(instrTask + " " + instrPerformer);

        //Place in onResume or where-ever one expects to receive messages from pebble
        pebbleDataReceiverReceiver = new PebbleKit.PebbleDataReceiver(Pebble_UUID) {

            //Function for receiving data from Pebble
            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary data) {
                //ACK the message
                PebbleKit.sendAckToPebble(context, transactionId);

                //Check the key exists
                if(data.getUnsignedIntegerAsLong( KEY_GESTURE) != null) {
                    int button = data.getUnsignedIntegerAsLong(KEY_GESTURE).intValue();

                    // Get random instruction for activeInstructions and send to phone
                    Random r = new Random();
                    int index = r.nextInt(activeInstructions.size());
                    String instrTask = ((Instruction)activeInstructions.get(index)).task;
                    String instrPerformer = ((Instruction)activeInstructions.get(index)).performer;
                    tvInstructions.setText(instrTask + " " + instrPerformer);

                    switch(button) {
                        case GESTURE_1:
                            //Insert Instructions here upon receiving a gesture 1 (please empty)
                            //tvInstructions.setText("Gesture 1 Done!, sending a round start");
                            String role= chooseRandomRole();
                            sendRoundStartToWatch(role);
                            break;
                        case GESTURE_2:
                            //Insert Instructions here upon receiving a gesture 2 (please empty)
                            //tvInstructions.setText("Gesture 2 Done! sending a phase");
                            if (score>=100) {
                                sendPhaseToWatch(FINAL_SCREEN, 100);
                            } else {
                                sendPhaseToWatch(WAITING_ROOM_SCREEN, score);
                            }
                            break;
                        case GESTURE_3:
                            //Insert Instructions for gesture 3 (please empty)
                            //tvInstructions.setText("Gesture 3 Done!, increasing score");
                            score+=10;
                            sendScoreToWatch(score);
                            break;
                    }

                    // Update score on screen
                    tvScore.setText("SCORE: " + score);
                }
            }
        };

        PebbleKit.registerReceivedDataHandler(this, pebbleDataReceiverReceiver);
        PebbleKit.registerReceivedAckHandler(getApplicationContext(), new PebbleKit.PebbleAckReceiver(Pebble_UUID) {

            @Override
            public void receiveAck(Context context, int transactionId) {
                Log.i(getLocalClassName(), "Received ack for transaction " + transactionId);
            }

        });

        PebbleKit.registerReceivedNackHandler(getApplicationContext(), new PebbleKit.PebbleNackReceiver(Pebble_UUID) {
            //resends message if it isn't received.
            @Override
            public void receiveNack(Context context, int wrongedId) {
                Log.i(getLocalClassName(), "Received nack for transaction " + wrongedId);
                if (wrongedId >= 0 && wrongedId <lastMessage.length) {
                    PebbleDictionary message= lastMessage[wrongedId];
                    if(message!=null) {
                        PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), Pebble_UUID, message, wrongedId);
                    }
                }
            }

        });

        // Update score on screen
        tvScore.setText("SCORE: " + score);

        //End of pebble code for Game Play
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Don't receive data from Pebble
        unregisterReceiver(pebbleDataReceiverReceiver);
        stopWatchApp();
    }

    //Function to start up Watch (ideally when app started up)
    public void startWatchApp() {
        PebbleKit.startAppOnPebble(getApplicationContext(), Pebble_UUID);
        PebbleDictionary data = new PebbleDictionary();
        data.addInt32(KEY_SEND_PHASE, WAITING_ROOM_SCREEN);
        sendDataToWatch(data);
    }

    //Function to stop Watch (ideally when app is stopped)
    // Send a broadcast to close the specified application on the connected Pebble
    public void stopWatchApp() {
        PebbleKit.closeAppOnPebble(getApplicationContext(), Pebble_UUID);
    }

    //Three functions to send info to watch:
    //Round start: send game play screen and role
    //Change Phase: sends phase change and score
    //sendScoreToWatch: just sends score
    public void sendRoundStartToWatch(String role) {
        PebbleDictionary data = new PebbleDictionary();
        data.addInt32(KEY_SEND_PHASE, GAME_PLAY_SCREEN);
        data.addString(KEY_SEND_ROLE, role);
        sendDataToWatch(data);
    }

    public void sendPhaseToWatch(int phase, int score) {
        PebbleDictionary data = new PebbleDictionary();
        data.addInt32(KEY_SEND_PHASE, phase);
        data.addInt32(KEY_SCORE_UPDATE, score);
        sendDataToWatch(data);
    }

    public void sendScoreToWatch(int score) {
        PebbleDictionary data = new PebbleDictionary();
        data.addInt32(KEY_SCORE_UPDATE, score);
        sendDataToWatch(data);
    }

    private void sendDataToWatch(PebbleDictionary data) {
        lastMessage[transactionID]=data;
        PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), Pebble_UUID, data, transactionID);
        transactionID++;
        if (transactionID>2) transactionID=0;
    }

    // Function to select and return the string of a random ROLE in the ROLE array
    private String chooseRandomRole() {
        Random r = new Random();
        int index = r.nextInt(roles.length);
        return roles[index];
    }

    // Function to select and return the string of a random TASK in the TASK array
    private String chooseRandomTask() {
        Random r = new Random();
        int index = r.nextInt(tasks.length);
        return tasks[index];
    }
}
