'''
Created on 23 okt. 2012

@author: Boris
'''

from poker import Poker

class Bot(object):
    '''
    In order to evaluate poker hand strength, I recommend
    checking out this:
    https://github.com/aliang/pokerhand-eval
    '''
    
    def make_move(self, poker, time_out):
        '''
        Define this function to make your bot work
        '''
        return 'raise %d' % (poker.pot_size()+2*poker.current_bet())

    def __init__(self):
        '''
        Constructor, add stuff if you like
        '''
        pass

if __name__ == '__main__':
    bot = Bot()
    poker = Poker()
    poker.run(bot)
