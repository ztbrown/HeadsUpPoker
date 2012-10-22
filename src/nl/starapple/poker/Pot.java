package nl.starapple.poker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class Pot is used for keeping track of the pot size, both from the main pot and
 * the possible side pots, and the players that are involved in the side pots.
 */
public class Pot
{
	private Map<PokerBot, Integer> botBetSizes;
	private int totalPot;
	
	/**
	 * Creates a Pot object, used for keeping track of the pot for a specific hand.
	 * @param bots : integer array of bot IDs that play in the current hand
	 */
	public Pot(Collection<PokerBot> bots)
	{
		botBetSizes = new HashMap<PokerBot, Integer>();
		Iterator<PokerBot> botIterator = bots.iterator();
		while(botIterator.hasNext())
			botBetSizes.put(botIterator.next(), 0);
		
		totalPot = 0;
	}
	
	
	/**
	 * Stores the bet of a bot.
	 */
	public void addBet(PokerBot bot, int size)
	{
		botBetSizes.put(bot, botBetSizes.get(bot) + size);
		totalPot += size;
	}
	
	
	/**
	 * Return value of function payoutWinners.
	 */
	public class PayoutWinnerInfo
	{
		private ArrayList<Integer> pots;
		private ArrayList<ArrayList<PokerBot>> winnerPerPot;
		public PayoutWinnerInfo(ArrayList<Integer> pots, ArrayList<ArrayList<PokerBot>> winnerPerPot)
		{
			super();
			this.pots = pots;
			this.winnerPerPot = winnerPerPot;
		}
		public ArrayList<Integer> getPots() {
			return pots;
		}
		public ArrayList<ArrayList<PokerBot>> getWinnerPerPot() {
			return winnerPerPot;
		}
	}
	
	/**
	 * Calculates for all the bots which pots they win. It first calculates which main pot and side pots there are.
	 * Then it computes which bot(s) win which pot. The returned ArrayList contains three objects. The first object is
	 * an ArrayList of the pot sizes represented as integers. The second object is an ArrayList of winners per pot,
	 * where each element is itself an ArrayList of winners of the corresponding pot.
	 * @param botHandStrengths : A map of PokerBots paired with their corresponding hand strengths
	 */
	public PayoutWinnerInfo payoutWinners(HashMap<PokerBot, Integer> botHandStrengths)
	{	
		// Calculate with the involved bots how much each bot put in the main pot and how much per side pot
		ArrayList<Integer> involvedBotBets = new ArrayList<Integer>();
		for(Entry<PokerBot, Integer> entry : botHandStrengths.entrySet())
			involvedBotBets.add(botBetSizes.get(entry.getKey()));
		Collections.sort(involvedBotBets);
		ArrayList<Integer> potsAmountPerBot = new ArrayList<Integer>();
		int previousAmount = 0;
		for(int i = 0; i < involvedBotBets.size(); i++)
		{
			potsAmountPerBot.add(involvedBotBets.get(i) - previousAmount);
			previousAmount = involvedBotBets.get(i);
		}
		
		// Get the sizes of the main pot and the side pots
		ArrayList<PokerBot> bots = new ArrayList<PokerBot>(botHandStrengths.keySet());
		ArrayList<Integer> pots = getPots(bots);
		
		// Calculate per pot part which players are winning it
		ArrayList<ArrayList<PokerBot>> winnerPerPot = new ArrayList<ArrayList<PokerBot>>();
		int potIndex = 0;
		int sumHandledPots = 0;
		while(botHandStrengths.size() > 0)
		{		
			// Get out of the remaining bots the bot(s) that has/have the best hand
			int bestHandValue = 0;
			ArrayList<PokerBot> currentBestBots = new ArrayList<PokerBot>();
			for(Entry<PokerBot, Integer> entry : botHandStrengths.entrySet())
			{
				int value = entry.getValue();
				if(value > bestHandValue)
					currentBestBots.clear();
				if(value >= bestHandValue)
				{
					bestHandValue = value;
					currentBestBots.add(entry.getKey());
				}					
			}
				
			// Calculate for each bot with currently the best hand in which remaining pots he is involved
			int maxPotIndex = 0;
			int maxSumHandledPots = 0;
			for(int i = 0; i < currentBestBots.size(); i++)
			{
				int currentPotIndex = potIndex;
				int currentSumHandledPots = sumHandledPots;
				PokerBot currentBot = currentBestBots.get(i);
				botHandStrengths.remove(currentBot);
				while(botBetSizes.get(currentBot) > currentSumHandledPots)
				{
					ArrayList<PokerBot> currentPotWinners = new ArrayList<PokerBot>();
					if(currentPotIndex <= winnerPerPot.size() - 1)
					{
						currentPotWinners = winnerPerPot.get(currentPotIndex);
						currentPotWinners.add(currentBot);
						winnerPerPot.set(currentPotIndex, currentPotWinners);
					}
					else
					{
						currentPotWinners.add(currentBot);
						winnerPerPot.add(currentPotWinners);
					}
					//currentSumHandledPots += pots.get(currentPotIndex++);
					currentSumHandledPots += potsAmountPerBot.get(currentPotIndex++);
				}
				maxPotIndex = Math.max(maxPotIndex, currentPotIndex);
				maxSumHandledPots = Math.max(maxSumHandledPots, currentSumHandledPots);
			}
			potIndex = maxPotIndex;
			sumHandledPots = maxSumHandledPots;
		}
		
		return new PayoutWinnerInfo(pots, winnerPerPot);
	}
	
	
	/**
	 * Calculates the amount of the pot that the winner receives. If it is about winning a side pot, the amount that
	 * the player betted more than the next better hand can be given in parameter 'maxSize'.
	 * @param bot : the winning bot
	 * @param maxSize : the maximal amount of chips per players bet that goes to this winner, any value <= 0 is
	 * interpreted as an unrestricted amount that may go to this player.
	 */
	/*
	public int getWinnersPot(PokerBot bot, int maxSize)
	{
		int winnersBet = botBetSizes.get(bot);
		if(maxSize > 0 && maxSize < winnersBet)
			winnersBet = maxSize;
		int winnersPot = 0;
		for(Entry<PokerBot, Integer> entry : botBetSizes.entrySet())
		{
		    PokerBot key = entry.getKey();
		    Integer value = entry.getValue();
		    
		    // If a player put in more chips than the winning player
			if(value > winnersBet)
			{
				botBetSizes.put(key, value - winnersBet);
				winnersPot += winnersBet;
				totalPot -= winnersBet;
			}
			// if a player put in equal or less chips than the winning player
			else
			{
				botBetSizes.put(key, 0);
				winnersPot += value;
				totalPot -= value;
			}
		}
		
		return winnersPot;
	}
	*/
	
	/**
	 * Calculates the amount of the pot that the given two winners receive. Returns an array of two integers pot sizes,
	 * the first one is the amount the first given bot wins, the second one is the amount the second bot wins. The main
	 * pot is split in half and if one bot put in more than the other, he gets this side pot back.
	 * @param bot1 : The first winning bot
	 * @param bot2 : The second winning bot
	 */
	/*
	public int[] getTwoWinnersPot(PokerBot bot1, PokerBot bot2)
	{
		int difference = botBetSizes.get(bot1) - botBetSizes.get(bot2);
		int[] botPots = new int[2];
		if(difference > 0)
			botPots[0] = getWinnersPot(bot1, difference);
		else if(difference < 0)
			botPots[1] = getWinnersPot(bot2, difference);
		int mainPot = getWinnersPot(bot1, difference);
		int mainPotHalf = mainPot / 2;
		botPots[1] += mainPotHalf;
		botPots[0] += mainPot - mainPotHalf;
		return botPots;
	}
	*/
	
	/**
	 * Returns whether the pot is empty or not
	 */
	public boolean isEmpty()
	{
		return totalPot == 0;
	}
	
	
	/**
	 * Returns the total size of the pot
	 */
	public int getTotalPotSize()
	{
		return totalPot;
	}
	
	
	/**
	 * Returns the size of the main pot and possible side pots, given the bots that are still involved in the hand.
	 * @param involvedBots : the bots that are still in the hand
	 */
	public ArrayList<Integer> getPots(ArrayList<PokerBot> involvedBots)
	{
		ArrayList<Integer> pots = new ArrayList<Integer>();
		Map<PokerBot, Integer> tempBotBetSizes = new HashMap<PokerBot, Integer>(botBetSizes);
		while(involvedBots.size() > 0)
		{
			int lowestBet = Integer.MAX_VALUE;
			int currentPotSize = 0;
			for(int i = 0; i < involvedBots.size(); i++)
				lowestBet = Math.min(tempBotBetSizes.get(involvedBots.get(i)), lowestBet);
			
			for(Entry<PokerBot, Integer> entry : tempBotBetSizes.entrySet())
			{
			    PokerBot key = entry.getKey();
			    int value = entry.getValue();
			    int adjustment = Math.min(value, lowestBet);
			    currentPotSize += adjustment;
				tempBotBetSizes.put(key, value - adjustment);
				if(value == adjustment)
					involvedBots.remove(key);				
			}
			
			pots.add(currentPotSize);
		}
		return pots;
	}
}
