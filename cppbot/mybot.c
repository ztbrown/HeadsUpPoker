#include <stdio.h>

#include "HandEval.h"
#include "mybot.h"

static const char *handNames[] = {
    "highcard",
    "one pair",
    "two pair",
    "three of a kind",
    "straight",
    "flush",
    "full house",
    "four of a kind",
    "straight flush",
    NULL };

void go(struct PokerState *state) {
    if( state->boardsize >= 3 ) {
        int strength_type = (state->hero.handstrength>>RESULT_VALUE_SHIFT);
        fprintf(stderr, "I have %s\n", handNames[strength_type]);
        if( strength_type >= state->boardsize-2 ) {
            printf("raise %d\n", state->potsize + state->currentbet);
            fflush(stdout);
            return;
        }
    }
    puts("call 0");
    fflush(stdout);
}

void setup(struct PokerState *state) {
    puts("ready");
    fflush(stdout);
}

