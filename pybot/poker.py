'''
Created on 23 okt. 2012

@author: Boris
'''

import sys

class Poker(object):
    '''
    Keeps the poker state, and contains a parser in the
    run method.
    '''

    def get_match_info(self, name):
        return self.match_info[name]

    def get_setting(self, name):
        return self.setting[name]
    
    def my_name(self):
        return self.get_setting('yourBot')

    def current_round(self):
        return int(self.get_match_info('round'))
    
    def small_blind(self):
        return int(self.get_match_info('smallBlind'))
    
    def big_blind(self):
        return int(self.get_match_info('bigBlind'))
    
    def is_my_button(self):
        '''
        True, if we act last post-flop
        '''
        return self.get_match_info('onButton') == self.my_name()
    
    def opponents(self):
        '''
        Will only contain one element if playing heads-up
        '''
        return [bot for _,bot in self.seat.items() if bot != self.my_name()]
    
    def opponent(self):
        return self.opponents()[-1]

    def get_stack(self, player):
        return self.stack[player]
    
    def my_stack(self):
        return self.get_stack(self.my_name())
    
    def opponent_stack(self):
        return self.get_stack(self.opponent())
    
    def pot_size(self):
        return int(self.get_match_info('pot'))
    
    def opponent_action(self):
        return self.last_action[self.opponent()]
    
    def opponent_amount(self):
        return self.last_amount[self.opponent()]

    def current_bet(self):
        '''
        Assumption: the amount to call is the last sidepot
        '''
        sidepots = Poker.split_list(self.get_match_info('sidepots'))
        if len(sidepots) == 0:
            return 0
        return int(sidepots[0])
    
    def get_hand(self, player):
        return self.hand[player]
    
    def my_hand(self):
        return Poker.split_list(self.get_hand(self.my_name()))
    
    def table_cards(self):
        return Poker.split_list(self.get_match_info('table'))

    def run(self, bot):
        '''
        Main-loop to parse input
        '''
        while not sys.stdin.closed:
            try:
                rawline = sys.stdin.readline()
                if len(rawline) == 0:
                    break
                line = rawline.strip()
                if len(line) == 0:
                    continue
                parts = line.split()
                cmd = parts[0]
                if len(parts) == 2 and cmd == 'go':
                    time_out = int(parts[1])
                    move = bot.make_move(self, time_out)
                    sys.stdout.write(move.strip() + "\n");
                    sys.stdout.flush()
                elif len(parts) != 3:
                    sys.stderr.write('Unable to parse line "%s"\n' % (line))
                    continue
                elif parts[0] == 'Settings':
                    self.setting[parts[1]] = parts[2]
                elif parts[0] == 'Match':
                    self.match_info[parts[1]] = parts[2]
                    if parts[1] == 'round':
                        self.match_info['table'] = '[]'
                elif parts[1] == 'stack':
                    self.stack[parts[0]] = int(parts[2])
                elif parts[1] == 'hand':
                    self.hand[parts[0]] = parts[2]
                elif parts[1] == 'seat':
                    self.seat[int(parts[2])] = parts[0]
                elif parts[1] in ['post', 'fold', 'check', 'call', 'raise', 'wins']:
                    # It's safe to ignore these
                    self.last_action[parts[0]] = parts[1]
                    self.last_amount[parts[0]] = int(parts[2])
                else:
                    sys.stderr.write('Unable to understand line "%s"\n' % (line))
            except EOFError:
                return
            except KeyboardInterrupt:
                # This only happens when testing
                print 'Ctrl-c pressed, quitting...'
                return
            except:
                # Better die
                raise

    def __init__(self):
        '''
        Constructor, bot parameter goes directly into run method
        '''
        # Initialize fields
        self.setting = { }
        self.match_info = { }
        self.stack = { }
        self.hand = { }
        self.last_action = { }
        self.last_amount = { }
        self.seat = { }

    @staticmethod
    def split_list(arg, delim=','):
        content = arg.strip('[]')
        if len(content) == 0:
                return []
        return content.split(delim)
