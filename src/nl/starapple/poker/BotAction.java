package nl.starapple.poker;
/**
 * Class that can be used to ask a bot to perform the next action within a certain amount of time. If the time
 * criterium is not met, it will return a null PokerMove.
 */
public class BotAction
{
	private PokerMove move = null;
	
	/**
	 * Queries and returns the next move of the given bot within the given time span. If the bot does not respond in
	 * time, null will be returned. 
	 * @param bot : the bot that has to take the next action
	 * @param timeOut : amount of time in milliseconds that the bot has to react
	 */
	public PokerMove getMove(final Robot bot, final int stack, final int totalPot, final long timeOut)
	{		
		Thread actionThread = new Thread()
		{
	        public void run()
	        {
	        	move = bot.getMove(stack, totalPot, timeOut);
	        }
		};
		
		actionThread.start();
		try{ actionThread.join(timeOut); }
			catch(InterruptedException e){ e.printStackTrace(); }
		
		return move;
	}
	
	
	/**
	 * Queries the given bot to setup within the given time span. The bot returns true when ready with its startup. If
	 * the bot does not respond in time, 'false' will be returned. 
	 * @param bot : the bot that has to prepare for the match
	 * @param timeOut : amount of time in milliseconds that the bot has to react
	 */
	public void setup(final Robot bot, final long timeOut)
	{		
		Thread actionThread = new Thread()
		{
	        public void run()
	        {
	        	bot.setup(timeOut);
	        }
		};
		
		actionThread.start();
		try{ actionThread.join(timeOut); }
			catch(InterruptedException e){ e.printStackTrace(); }
	}
}
