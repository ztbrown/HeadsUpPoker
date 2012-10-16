package nl.starapple.poker;
import java.util.ArrayList;

public class HandInfo
{
	private HandInfoType infoType;
	private int round;
	private ArrayList<PokerBot> bots;
	private int[] botStacks;
	private int sizeBB, sizeSB;
	private PokerBot myBot;
	private Hand myHand;
	private int buttonSeat;
	private String table;
	private int pot;
	
	public HandInfo(HandInfoType type, int roundNumber, ArrayList<PokerBot> botList, int[] stacks, int bigBlindSize, int smallBlindSize,
					 int button, String tableCards, int potSize)
	{
		infoType = type;
		round = roundNumber;
		bots = botList;
		botStacks = stacks;
		sizeBB = bigBlindSize;
		sizeSB = smallBlindSize;
		buttonSeat = button;
		table = tableCards;
		pot = potSize;
	}
	
	
	/**
	 * Sets on which seat the bot is that receives this MatchInfo
	 * @param botName : the name of the bot
	 */
	public void setCurrentBotInfo(PokerBot bot, Hand hand)
	{
		if(!bots.contains(bot))
			System.err.println("The given bot is not part of this match!");
		myBot = bot;
		myHand = hand;
	}
	
	
	/**
	 * Returns a String representation of the current table situation.
	 */
	public String toString()
	{
		String str = "";
		if(infoType.equals(HandInfoType.HandStart))
		{
			str += String.format("Match round %d\n", round);
			str += String.format("Match smallBlind %d\n", sizeSB);
			str += String.format("Match bigBlind %d\n", sizeBB);
		}
		for(int i = 0; i < bots.size(); i++)
			str += String.format("%s stack %d\n", bots.get(i).getName(), botStacks[i]);
		if(infoType.equals(HandInfoType.HandStart))
		{
			str += String.format("Match onButton %s\n", bots.get(buttonSeat).getName());
			str += String.format("%s hand %s\n", bots.get(buttonSeat).getName(), myHand.toString());
		}
		str += String.format("Match pot %d\n", pot);
		str += String.format("Match table %s", table);
		return str;
	}
}
