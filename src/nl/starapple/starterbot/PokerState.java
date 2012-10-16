package nl.starapple.starterbot;

import nl.starapple.poker.Card;
import nl.starapple.poker.Hand;

public class PokerState {
	
	private int round, smallBlind, bigBlind, button, seat;
	
	private int yourStack, opponentStack;
	
	private String opponentAction;
	
	private int currentBet;
	
	private Hand hand;
	
	private Card[] table;

	protected void updateMatch(String key, String value) {
		if( key.equals("round") ) {
			round = Integer.valueOf(value);
		} else if( key.equals("smallBlind") ) {
			smallBlind = Integer.valueOf(value);
		} else if( key.equals("bigBlind") ) {
			bigBlind = Integer.valueOf(value);
		} else if( key.equals("button") ) {
			button = Integer.valueOf(value);
		} else if( key.equals("table") ) {
			table = parseCards(value);
		} else {
			System.err.printf("Unknown match command: %s %s\n", key, value);
		}
	}

	protected void updateMove(String bot, String move, String amount) {
		if( bot.equals("You") ) {
			if( move.equals("stack") ) {
				yourStack = Integer.valueOf(amount);
			} else if( move.equals("seat") ) {
				seat = Integer.valueOf(amount);
			} else if( move.equals("hand") ) {
				Card[] cards = parseCards(amount);
				assert( cards.length == 2 ) : String.format("Did not receive two cards, instead: ``%s''", amount);
				hand = new Hand(cards[0], cards[1]);
			} else {
				// assume we made some move
			}
		} else {
			// assume it's the (only) villain
			if( move.equals("stack") ) {
				opponentStack = Integer.valueOf(amount);
			} else {
				// assume he made some move
				try {
					currentBet = Integer.valueOf(amount);
					opponentAction = move;
				} catch( NumberFormatException e ) {
					System.err.printf("Unable to parse line ``%s %s %s''\n", bot, move, amount);
				}
			}
		}
	}

	private Card[] parseCards(String value) {
		if( value.endsWith("]") ) { value = value.substring(0, value.length()-1); }
		if( value.startsWith("[") ) { value = value.substring(1); }
		String[] parts = value.split(",");
		Card[] cards = new Card[parts.length];
		for( int i = 0; i < parts.length; ++i ) {
			cards[i] = Card.getCard(parts[i]);
		}
		return cards;
	}

	public int getRound() {
		return round;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public int getButton() {
		return button;
	}

	public int getSeat() {
		return seat;
	}

	public int getYourStack() {
		return yourStack;
	}

	public int getOpponentStack() {
		return opponentStack;
	}
	
	public String getOpponentAction() {
		return opponentAction;
	}
	
	public int getCurrentBet() {
		return currentBet;
	}

	public Hand getHand() {
		return hand;
	}

	public Card[] getTable() {
		return table;
	}

}
