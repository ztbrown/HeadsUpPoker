package nl.starapple.poker;
import java.util.ArrayList;

public class MatchInfo
{
	private MatchInfoType infoType;
	private int round;
	private ArrayList<PokerBot> bots;
	private int[] botStacks;
	private int sizeBB, sizeSB;
	private int mySeat;
	private String myHand;
	private int buttonSeat;
	private String table;
	private int pot;
	
	public MatchInfo(MatchInfoType type, int roundNumber, ArrayList<PokerBot> botList, int[] stacks, int bigBlindSize, int smallBlindSize,
					 int button, String tableCards, int potSize)
	{
		infoType = type;
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
		String str = "";
		if(infoType.equals(MatchInfoType.HandStart))
		{
			str += String.format("Match round %d\n", round);
			str += String.format("Match smallBlind %d\n", sizeSB);
			str += String.format("Match bigBlind %d\n", sizeBB);
			str += String.format("Match button %d\n", buttonSeat);
		}
		for(int i = 0; i < bots.size(); i++)
		{
			if(i != mySeat)
				str += String.format("%s stack %d\n", bots.get(i).getName(), botStacks[i]);
			else
				str += String.format("You stack %d\n", botStacks[i]);
		}
		if(infoType.equals(MatchInfoType.HandStart))
		{
			str += String.format("You seat %d\n", mySeat);
			str += String.format("You hand %s\n", myHand);
		}
		str += String.format("Match pot %d\n", pot);
		str += String.format("Match table %s", table);
		return str;
	}
}
