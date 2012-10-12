import java.util.ArrayList;

public class MatchInfo
{
	private int round;
	private ArrayList<PokerBot> bots;
	private int[] botStacks;
	private String table;
	private int sizeBB, sizeSB;
	private int mySeat;
	private int buttonSeat;
	
	public MatchInfo(int roundNumber, ArrayList<PokerBot> botList, int[] stacks, int bigBlindSize, int smallBlindSize, int button, String tableCards)
	{
		round = roundNumber;
		bots = botList;
		botStacks = stacks;
		table = tableCards;
		sizeBB = bigBlindSize;
		sizeSB = smallBlindSize;
		mySeat = -1;
		buttonSeat = button;
	}
	
	
	/**
	 * Sets on which seat the bot is that receives this MatchInfo
	 * @param botName : the name of the bot
	 */
	public void setCurrentBotSeat(PokerBot bot)
	{
		for(int i = 0; i < bots.size(); i++)
		{
			if(bots.get(i).equals(bot))
			{
				mySeat = i;
				break;
			}
		}
	}
	
	
	/**
	 * Returns a String representation of the current table situation
	 */
	public String toString()
	{
		String str = String.format("Match round %d\n", round);
		str += String.format("Match SB %d\n", sizeSB);
		str += String.format("Match BB %d\n", sizeBB);
		str += String.format("Match DB %d\n", buttonSeat);
		for(int i = 0; i < bots.size(); i++)
		{
			if(i != mySeat)
				str += String.format("%d stack %d\n", bots.get(i).getName(), botStacks[i]);
			else
				str += String.format("You stack %d\n", botStacks[i]);
		}
		str += String.format("Match table %d", table);
		return str;
	}
}
