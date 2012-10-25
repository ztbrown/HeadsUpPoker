package nl.starapple.poker;

import java.util.ArrayList;

public class HandResultInfo
{
	private ArrayList<PokerBot> bots;
	private int[] potParts;
	private Hand[] hands;
	
	public HandResultInfo(ArrayList<PokerBot> botList, int[] potDistribution)
	{
		bots = botList;
		potParts = potDistribution;
		hands = new Hand[bots.size()];
	}
	
	
	/**
	 * Sets the hand of a bot that is involved in the showdown.
	 * @param botName : the name of the bot
	 * @param hand : the hand of the bot
	 */
	public void setBotHand(int seat, Hand hand)
	{
		if(seat >= bots.size() || seat < 0)
			System.err.println("The given bot is not part of this match!");
		
		hands[seat] = hand;
	}
	
	
	/**
	 * Returns a String representation of the match result information.
	 */
	public String toString()
	{
		String str = "";
		for(int i = 0; i < bots.size(); i++)
			if(hands[i] != null)
				str += String.format("bot_%d hand %s\n", i, hands[i].toString());
		for(int i = 0; i < bots.size(); i++)
			if(potParts[i] > 0)
				str += String.format("bot_%d wins %d\n", i, potParts[i]);
		str = str.substring(0, str.length() - 1);
		return str;
	}
}
