package nl.starapple.starterbot;

import nl.starapple.poker.Hand;
import nl.starapple.poker.PokerMove;

public class BotStarter implements Bot {

	@Override
	public PokerMove getMove(PokerState state, Long timeOut) {
		Hand hand = state.getHand();
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
