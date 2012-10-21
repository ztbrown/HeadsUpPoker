#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stdlib.h>

#include "mybot.h"

static Hand_T_Rank_Suit_Table(handtable);

static int split(char *str, char **part, int maxparts);
static void parse_setting(const char *key, const char *value, struct PokerSettings *settings);
static void parse_matchinfo(const char *key, const char *value, struct PokerState *state);
static void parse_playerinfo(const char *key, const char *value, struct PokerPlayer *player);
static Hand_T parse_cards(const char *str);
static Hand_T parse_card(const char *str);
static int count_cards(Hand_T cards);
static Eval_T get_handstrength(Hand_T cards1, Hand_T cards2);
static void player_store_handstrength(struct PokerState *state, struct PokerPlayer *player);

int main() {
    char buf[100];
    struct PokerState state;
    memset(&state, 0, sizeof(state));
    if( !Init_Hand_Eval() ) {
        fprintf(stderr, "ERR: unable to Init_Hand_Eval\n");
        return 1;
    }
    while( fgets(buf, sizeof(buf), stdin) != NULL ) {
        if( buf[0] == '\n' ) { continue; }
        const int maxparts = 10;
        char *part[maxparts];
        int parts = split(buf, part, maxparts);
        if( parts == 3 && strcmp(part[0], "Settings") == 0 ) {
            parse_setting(part[1], part[2], &state.settings);
        } else if( parts == 3 && strcmp(part[0], "Match") == 0 ) {
            parse_matchinfo(part[1], part[2], &state);
        } else if( parts == 3 && state.settings.myname != NULL ) {
            struct PokerPlayer *player =
                strcmp(part[0], state.settings.myname)
                ? &state.villain
                : &state.hero;
            if( player->name == NULL ) {
                player->name = strdup(part[0]);
            }
            if( strcmp(player->name, part[0]) != 0 ) {
                fprintf(stderr, "WARN: ``%s'' does not match villain name ``%s''\n", part[0], player->name);
            } else {
                parse_playerinfo(part[1], part[2], player);
                if( strcmp(part[1], "check") == 0 ||
                        strcmp(part[1], "call") == 0 ) {
                    state.currentbet = 0;
                } else if( strcmp(part[1], "raise") == 0 ) {
                    state.currentbet = player->lastamount;
                } else if( strcmp(part[1], "post") == 0 ) {
                    /*
                     * this will make sure that currentbet is
                     * bigblind-smallblind, which is the amount
                     * to call, since currentbet is set to 0 when
                     * we get ``Match round x''
                     */
                    state.currentbet = player->lastamount - state.currentbet;
                } else if( strcmp(part[1], "hand") == 0 ) {
                    player_store_handstrength(&state, player);
                }
            }
        } else if( parts == 2 && strcmp(part[0], "go") == 0 ) {
            player_store_handstrength(&state, &state.hero);
            state.timeout = atol(part[1]);
            go(&state);
            fflush(stdout);
        } else if( parts == 2 && strcmp(part[0], "setup") == 0 ) {
            state.timeout = atol(part[1]);
            setup(&state);
            fflush(stdout);
        } else {
            int i;
            fprintf(stderr, "WARN: unable to understand %d part command%s: ``",
                parts, (state.settings.myname?"":" (don't know my name yet)"));
            for( i = 0; i < parts; ++i ) {
                fprintf(stderr, (i==0)?"%s":" %s", part[i]);
            }
            fprintf(stderr, "''\n");
        }
    }
    return 0;
}

static int split(char *str, char **part, int maxparts) {
    char *start = str;
    int parts = 0;
    while( isspace(*str) ) { ++str; }
    if( *str == '\0' ) { return 0; }
    while( parts < maxparts && *str != '\0' ) {
        while( *str != '\0' && !isspace(*str) ) { ++str; }
        part[parts++] = start;
        while( isspace(*str) ) { *str++ = '\0'; }
        start = str;
    }
    return parts;
}

static void parse_setting(const char *key, const char *value, struct PokerSettings *settings) {
    if( strcmp(key, "gameType") == 0 ) {
        settings->gametype = strdup(value);
    } else if( strcmp(key, "gameMode") == 0 ) {
        settings->gamemode = strdup(value);
    } else if( strcmp(key, "timeBank") == 0 ) {
        settings->timebank = atol(value);
    } else if( strcmp(key, "timePerMove") == 0 ) {
        settings->timepermove = atol(value);
    } else if( strcmp(key, "handsPerLevel") == 0 ) {
        settings->handsperlevel = atoi(value);
    } else if( strcmp(key, "yourBot") == 0 ) {
        settings->myname = strdup(value);
    } else {
        fprintf(stderr, "WARN: unknown setting %s=%s\n", key, value);
    }
}

static void parse_matchinfo(const char *key, const char *value, struct PokerState *state) {
    if( strcmp(key, "round") == 0 ) {
        state->info.round = atoi(value);
        state->currentbet = 0;
    } else if( strcmp(key, "smallBlind") == 0 ) {
        state->info.smallblind = atoi(value);
    } else if( strcmp(key, "bigBlind") == 0 ) {
        state->info.bigblind = atoi(value);
    } else if( strcmp(key, "pot") == 0 ) {
        state->potsize = atoi(value);
    } else if( strcmp(key, "table") == 0 ) {
        state->board = parse_cards(value);
        state->boardsize = count_cards(state->board);
    } else if( strcmp(key, "onButton") == 0 ) {
        if( state->settings.myname == NULL ) {
            fprintf(stderr, "WARN: got ``onButton'', but don't know my name yet\n");
        } else {
            int mybutton = (strcmp(value,state->settings.myname)==0);
            state->hero.onbutton = mybutton;
            state->villain.onbutton = (!mybutton);
        }
    }
}

static void parse_playerinfo(const char *key, const char *value, struct PokerPlayer *player) {
    if( strcmp(key, "seat") == 0 ) {
        player->seat = atoi(value);
    } else if( strcmp(key, "hand") == 0 ) {
        player->hand = parse_cards(value);
    } else if( strcmp(key, "stack") == 0 ) {
        player->stack = atoi(value);
    } else {
        strncpy(player->lastaction, key, sizeof(player->lastaction));
        player->lastaction[sizeof(player->lastaction)-1] = '\0';
        player->lastamount = atoi(value);
    }
}

static Hand_T parse_card(const char *str) {
    int rank, suit;
    switch( str[0] ) {
        case '2': rank = 0; break;
        case '3': rank = 1; break;
        case '4': rank = 2; break;
        case '5': rank = 3; break;
        case '6': rank = 4; break;
        case '7': rank = 5; break;
        case '8': rank = 6; break;
        case '9': rank = 7; break;
        case 'T': rank = 8; break;
        case 'J': rank = 9; break;
        case 'Q': rank = 10; break;
        case 'K': rank = 11; break;
        case 'A': rank = 12; break;
        default: fprintf(stderr, "WARN: unknown card rank ``%c''\n", str[0]); return emptyHand;
    }
    switch( str[1] ) {
        case 'c': suit = 0; break;
        case 'd': suit = 1; break;
        case 'h': suit = 2; break;
        case 's': suit = 3; break;
        default: fprintf(stderr, "WARN: unknown card suit ``%c''\n", str[1]); return emptyHand;
    }
    return handtable[rank][suit];
}

static Hand_T parse_cards(const char *str) {
    if( str[0] != '[' ) {
        fprintf(stderr, "WARN: cannot parse cards string ``%s''\n", str);
        return emptyHand;
    }
    int len = strlen(str);
    Hand_T result = emptyHand;
    int i;
    for( i = 0; i+2 < len && str[i] != ']'; i += 3 ) {
        Hand_T card = parse_card(str+i+1);
        AddHandTo(result, card);
    }
    return result;
}

static int count_cards(Hand_T cards) {
    int count = 0;
#if HAVE_INT64
    uint64_t bits = cards.as64Bits;
    while( bits ) { count += (bits&1); bits >>= 1; }
#else
    int bits = cards.as2x32Bits.cd;
    while( bits ) { count += (bits&1); bits >>= 1; }
    bits = cards.as2x32Bits.hs;
    while( bits ) { count += (bits&1); bits >>= 1; }
#endif
    return count;
}

static Eval_T get_handstrength(Hand_T cards1, Hand_T cards2) {
    int numcards = count_cards(cards1) + count_cards(cards2);
    Hand_T cards; CombineHands(cards, cards1, cards2);
    switch( numcards ) {
    case 5: return Hand_5_Eval(cards);
    case 6: return Hand_6_Eval(cards);
    case 7: return Hand_7_Eval(cards);
    }
    fprintf(stderr, "WARN: unable to eval %d cards\n", numcards);
    return 0;
}

static void player_store_handstrength(struct PokerState *state, struct PokerPlayer *player) {
    if( state->boardsize == 0 ) { return; }
    if( IsEmptyHand(player->hand) ) { return; }
    player->handstrength = get_handstrength(player->hand, state->board);
}

