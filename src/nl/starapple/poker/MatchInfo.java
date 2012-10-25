package nl.starapple.poker;

import java.util.ArrayList;

public class MatchInfo
{
	private ArrayList<PokerBot> bots;
	private int mySeat;
	private String gameType;
	private String gameMode;
	private int timeBank;
	private int timePerMove;
	private int handsPerLevel;
	
	public MatchInfo(ArrayList<PokerBot> botList, String typeGame, boolean tournament, long bankTime, long moveTime,
					 int handsPerBlindLevel)
	{
		bots = botList;
		gameType = typeGame;
		if(tournament)
			gameMode = "tournament";
		else
			gameMode = "cashgame";
		timeBank = (int) bankTime;
		timePerMove = (int) moveTime;
		handsPerLevel = handsPerBlindLevel;
	}
	
	
	/**
	 * Sets on which seat the bot is that receives this MatchInfo
	 * @param botName : the name of the bot
	 */
	public void setCurrentBotInfo(int seat)
	{
		if(seat >= bots.size() || seat < 0)
			System.err.println("The given bot is not part of this match!");
		mySeat = seat;
	}
	
	
	/**
	 * Returns a String representation of the match information.
	 */
	public String toString()
	{
		String str = String.format("Settings gameType %s", gameType);
		str += String.format("\nSettings gameMode %s", gameMode);
		str += String.format("\nSettings timeBank %d", timeBank);
		str += String.format("\nSettings timePerMove %d", timePerMove);
		str += String.format("\nSettings handsPerLevel %d", handsPerLevel);
		str += String.format("\nSettings yourBot bot_%d", mySeat);
		for(int i = 0; i < bots.size(); i++)
			str += String.format("\nbot_%d seat %d", i, i);
		
		return str;
	}
}
