package nl.starapple.poker;
import java.util.ArrayList;

public class MatchInfo
{
	private int round;
	private ArrayList<PokerBot> bots;
	private int[] botStacks;
	private int sizeBB, sizeSB;
	private int mySeat;
	private String myHand;
	private int buttonSeat;
	private String table;
	private int pot;
	
	public MatchInfo(int roundNumber, ArrayList<PokerBot> botList, int[] stacks, int bigBlindSize, int smallBlindSize,
					 int button, String tableCards, int potSize)
	{
		round = roundNumber;
		bots = botList;
		botStacks = stacks;
		sizeBB = bigBlindSize;
		sizeSB = smallBlindSize;
		mySeat = -1;
		buttonSeat = button;
		table = tableCards;
		pot = potSize;
	}
	
	
	/**
	 * Sets on which seat the bot is that receives this MatchInfo
	 * @param botName : the name of the bot
	 */
	public void setCurrentBotInfo(PokerBot bot, String hand)
	{
		for(int i = 0; i < bots.size(); i++)
		{
			if(bots.get(i).equals(bot))
			{
				mySeat = i;
				break;
			}
		}
		myHand = hand;
	}
	
	
	/**
	 * Returns a String representation of the current table situation
	 */
	public String toString()
	{
		String str = String.format("Match round %d", round);
		str += String.format("\nMatch smallBlind %d", sizeSB);
		str += String.format("\nMatch bigBlind %d", sizeBB);
		str += String.format("\nMatch button %d", buttonSeat);
		for(int i = 0; i < bots.size(); i++)
		{
			if(i != mySeat)
				str += String.format("\n%s stack %d", bots.get(i).getName(), botStacks[i]);
			else
				str += String.format("\nYou stack %d", botStacks[i]);
		}
		str += String.format("\nYou seat %d", mySeat);
		str += String.format("\nYou hand %s", myHand);
		str += String.format("\nMatch pot %d", pot);
		str += String.format("\nMatch table %s", table);
		return str;
	}
}
