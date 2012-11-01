#ifndef _mybot_h
#define _mybot_h

#include "HandEval.h"

#define MAX_SIDEPOTS 8

struct PokerSettings {
    const char *gametype, *gamemode;
    const char *myname;
    long timebank, timepermove;
    int handsperlevel;
};

struct PokerPlayer {
    const char *name;
    int seat, onbutton;
    int stack;
    char lastaction[10];
    int lastamount;
    Hand_T hand;
    Eval_T handstrength;
};

struct MatchInfo {
    int round;
    int smallblind, bigblind;
};

struct PokerState {
    struct PokerSettings settings;
    struct PokerPlayer hero, villain;
    struct MatchInfo info;
    Hand_T board;
    int boardsize;
    int potsize, currentbet;
    int sidepot[MAX_SIDEPOTS], sidepots;
    long timeout;
};

void go(struct PokerState *state);
void setup(struct PokerState *state);

#endif /* _mybot_h */
