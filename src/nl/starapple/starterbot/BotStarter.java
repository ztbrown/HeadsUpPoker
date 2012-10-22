package nl.starapple.starterbot;

import nl.starapple.poker.Card;
import nl.starapple.poker.Hand;
import nl.starapple.poker.PokerMove;

import com.stevebrecher.HandEval;

public class BotStarter implements Bot {
	
	public HandEval.HandCategory rankToCategory(int rank) {
		return HandEval.HandCategory.values()[rank >> HandEval.VALUE_SHIFT];
	}
	
	public HandEval.HandCategory getHandCategory(Hand hand, Card[] table) {
		if( table.length == 0 ) {
			return hand.getCard1().getHeight() == hand.getCard2().getHeight()
					? HandEval.HandCategory.PAIR
					: HandEval.HandCategory.NO_PAIR;
		}
		long handCode = hand.getCard1().getNumber() + hand.getCard2().getNumber();
		for( Card card : table ) { handCode += card.getNumber(); }
		if( table.length == 3 ) {
			return rankToCategory(HandEval.hand5Eval(handCode));
		}
		if( table.length == 4 ) {
			return rankToCategory(HandEval.hand6Eval(handCode));
		}
		return rankToCategory(HandEval.hand7Eval(handCode));
	}

	@Override
	public PokerMove getMove(PokerState state, Long timeOut) {
		Hand hand = state.getHand();
		String handCategory = getHandCategory(hand, state.getTable()).toString();
		System.err.printf("my hand is %s, opponent action is %s (%d)\n", handCategory, state.getOpponentAction(), state.getCurrentBet());
		int height1 = hand.getCard1().getHeight().ordinal();
		int height2 = hand.getCard1().getHeight().ordinal();
		if( height1 > 9 || height2 > 9 ) {
			return new PokerMove("raise", 2*state.getBigBlind());
		} else if( height1 > 5 && height2 > 5 ) {
			return new PokerMove("call", 0);
		} else {
			return new PokerMove("check", 0);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Parser parser = new Parser(new BotStarter());
		parser.run();
	}

}
