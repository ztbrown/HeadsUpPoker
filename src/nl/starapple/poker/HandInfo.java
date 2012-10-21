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
	private ArrayList<Integer> pots;
	
	public HandInfo(HandInfoType type, int roundNumber, ArrayList<PokerBot> botList, int[] stacks, int bigBlindSize, int smallBlindSize,
					 int button, String tableCards, ArrayList<Integer> allPots)
	{
		infoType = type;
		round = roundNumber;
		bots = botList;
		botStacks = stacks;
		sizeBB = bigBlindSize;
		sizeSB = smallBlindSize;
		buttonSeat = button;
		table = tableCards;
		pots = allPots;
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
		if(infoType.equals(HandInfoType.HAND_CARDS))
			str += String.format("%s hand %s\n", myBot.getName(), myHand.toString());
		else
		{
			if(infoType.equals(HandInfoType.HAND_START))
			{
				str += String.format("Match round %d\n", round);
				str += String.format("Match smallBlind %d\n", sizeSB);
				str += String.format("Match bigBlind %d\n", sizeBB);
				str += String.format("Match onButton %s\n", bots.get(buttonSeat).getName());
			}
			
			if( infoType.equals(HandInfoType.NEW_BETROUND) ) {
				str += String.format("Match table %s\n", table);
			}
			
			if( infoType.equals(HandInfoType.PREMOVE_INFO) )
			{
				for(int i = 0; i < bots.size(); i++)
					str += String.format("%s stack %d\n", bots.get(i).getName(), botStacks[i]);
				
				str += String.format("Match pot %d\n", pots.get(0));
				str += String.format("Match sidepots [");
				for(int i = 1; i < pots.size(); i++)
					str += ((i>1)?",":"") + pots.get(i);
				str += "]\n";
			}
		}
		return str;
	}
}
