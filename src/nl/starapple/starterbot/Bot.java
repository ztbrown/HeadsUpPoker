package nl.starapple.starterbot;

import nl.starapple.poker.PokerMove;

public interface Bot {

	public PokerMove getMove(PokerState state, Long timeOut);

}
