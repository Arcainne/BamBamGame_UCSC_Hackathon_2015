#pragma once
#include "pebble.h"
enum {
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
    FINAL_SCREEN=13,
};



//send a gesture out to the phone
void sendGesture(int gestureKey);

