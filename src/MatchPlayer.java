import java.util.Arrays;
import java.util.Vector;


public class MatchPlayer {
	
	private int[] botIDs;
	private int numberOfBots;
	private Deck deck;
	private Pot pot;
	private Vector<Card> tableCards;
	private Hand[] botHands;
	private int[] botStacks;
	private int buttonSeat;
	private int activeSeat;
	private int lastToActSeat;
	private int lastToActWithoutRaiseSeat;
	private int sizeStartStack;
	private int sizeSB, sizeBB;
	private boolean[] isInvolvedInHand;
	private boolean[] isInvolvedInMatch;
	private boolean isTournament;
	private boolean raiseLock, raiseLockActive;
	private int[] botBetsThisRound;
	private int[] botGainLoss;
	
	
	/**
	 * Setup a table with the given bots, so that a match can be played.
	 * @param botID : an array of bot IDs that are on the table
	 * @param stack : the stack size that all bots start with
	 * @param BB : the size of the big blind
	 * @param SB : the size of the small blind
	 * @param tournamentMode : whether it is a tournament or not
	 */
	public MatchPlayer(int[] botID, int stack, int BB, int SB, boolean tournamentMode)
	{
		botIDs = botID;
		numberOfBots = botIDs.length;
		sizeStartStack = stack;
		botStacks = new int[numberOfBots];
		for(int i = 0; i < numberOfBots; i++)
		{
			botIDs[i] = botID[i];
			botStacks[i] = sizeStartStack;
		}
		
		sizeBB = BB;
		sizeSB = SB;
		pot = new Pot(botIDs);
		deck = new Deck();
		tableCards = new Vector<Card>();
		botHands = new Hand[numberOfBots];
		buttonSeat = numberOfBots;
		activeSeat = 0;
		isInvolvedInHand = new boolean[numberOfBots];
		isInvolvedInMatch = new boolean[numberOfBots];
		isTournament = tournamentMode;
		raiseLock = false;
		if(!isTournament)
			botGainLoss = new int[numberOfBots];
	}
	
	
	/**
	 * Prepares for playing the next hand, the dealer button is moved to the next bot and all bots are reset to being
	 * involved in the next hand. If the game is not a tournament then the gain or loss per bot on the last hand is
	 * stored and all stack sizes are reset to the standard starting stack and all bots are reset to being involved in
	 * the match.
	 */
	public void setupNextHand()
	{
		buttonSeat += 1;
		if(buttonSeat + 1 > numberOfBots)
			buttonSeat = 0;
		
		for(int i = 0; i < numberOfBots; i++)
			isInvolvedInHand[i] = true;
		
		if(!isTournament)
		{
			for(int i = 0; i < numberOfBots; i++)
			{
				botGainLoss[i] += botStacks[i] - sizeStartStack;
				botStacks[i] = sizeStartStack;
				isInvolvedInMatch[i] = true;
			}
		}		
	}
	
	
	/**
	 * Play the next hand of the match.
	 */
	public void playHand()
	{
		setupNextHand();
		payBlinds();
		dealHandCards();
		
		BetRound round = BetRound.PREFLOP;
		int numberOfBotsActiveInHand = 0;
		for(int i = 0; i < numberOfBots; i++)
			if(isInvolvedInHand[i])
				numberOfBotsActiveInHand++;
		int numberOfBotsInHand = numberOfBotsActiveInHand;
		
		boolean handFinished = false;
		while(!handFinished)
		{
			// if there is only one active bot left in the hand, but one or more others are all-in, then deal all
			// the table cards, there is then no action in further rounds.
			if(numberOfBotsInHand > 1 && numberOfBotsActiveInHand == 1){}
			else
			{
				int currentRaiseSize = 0;
				if(round.equals(BetRound.PREFLOP))
					currentRaiseSize = sizeBB;
				else
				{
					activeSeat = buttonSeat;
					nextBotActive();
				}
				
				int minimalRaiseSize = sizeBB;
				int lastRaiseSeat = activeSeat;
				int noRaiseAllowedFromSeat = activeSeat;
				boolean noRaiseAllowed = false;
				botBetsThisRound = new int[numberOfBots];
				
				boolean roundFinished = false;	
				while(!roundFinished)
				{
					if(isInvolvedInHand[activeSeat] && botStacks[activeSeat] > 0)
					{
						int amountToCall = currentRaiseSize - botBetsThisRound[activeSeat];
						String botActionString = (???)[botIDs[activeSeat]].makeBet();
						String[] botActionSubStrings = botActionString.split(",");
						String botAction = botActionSubStrings[0];
						int botActionAmount = Integer.parseInt(botActionSubStrings[1]);
						
						if(botAction.equals("call") || (botAction.equals("raise") && noRaiseAllowed))
						{
							int realCallSize = placeBet(amountToCall);
							if(botStacks[activeSeat] == 0)
								numberOfBotsActiveInHand--;
						}
						
						else if(botAction.equals("check"))
						{
							if(amountToCall > 0)
								isInvolvedInHand[activeSeat] = false;
						}
						
						else if(botAction.equals("raise"))
						{
							botActionAmount = Math.max(botActionAmount, minimalRaiseSize);
							int realBetAmount = placeBet(amountToCall + botActionAmount);					
							
							// when a bot raises, all other players must get a turn to respond with an action 
							if(realBetAmount >= amountToCall)
							{
								updateLastSeatToAct();
								// if the raise is a minimal raise or larger, the minimal raise size is updated
								if(realBetAmount >= amountToCall + minimalRaiseSize)
								{
									minimalRaiseSize = realBetAmount;
									lastRaiseSeat = activeSeat;
									raiseLock = false;
								}
								// if the raise is smaller than the minimal raise (an under-raise all-in), then the raise
								// is treated as a call, thereby not enabling all bots to reraise.
								else
								{
									noRaiseAllowedFromSeat = lastRaiseSeat;
									raiseLock = true;
								}						
								currentRaiseSize = botBetsThisRound[activeSeat];
							}
							// when a bot said to raise but has not even enough to call, then he calls all-in
							else
								numberOfBotsActiveInHand--;
						}
						// the default action is fold
						else
							isInvolvedInHand[activeSeat] = false;				
					}
					
					// if the current bot folded, the number of active bots in this hand is updated
					if(isInvolvedInHand[activeSeat] == false)
					{
						numberOfBotsActiveInHand--;
						numberOfBotsInHand--;
					}
					
					// only one bot is left in the hand, he wins the pot
					if(numberOfBotsInHand == 1)
					{
						... // give pot to winner, no showdown
					}
					
					// if no one has to act anymore, the bet round is over
					if(lastToActSeat == activeSeat || numberOfBotsActiveInHand == 1)
						roundFinished = true;
					else
					{				
						nextBotActive();
						if(raiseLock && noRaiseAllowedFromSeat == activeSeat)
							noRaiseAllowed = true;
					}
				}
			}
			if(dealNextStreet(round) == NULL)
				handFinished = true;
		}
		
		// showdown or one pot winner
	}
	
	
	/**
	 * Used to increment 'activeSeat' so that it becomes the position of the next bot.
	 */
	public void nextBotActive()
	{
		activeSeat += 1;
		if(activeSeat + 1 > numberOfBots)
			activeSeat = 0;
	}
	
	
	/**
	 * Updates which seat is the last seat that may act in the current round
	 */
	public void updateLastSeatToAct()
	{
		lastToActSeat = activeSeat - 1;
		if(lastToActSeat < 0)
			lastToActSeat = numberOfBots - 1;
	}
	
	
	/**
	 * Forces the next two players after the dealer button to pay the blinds. In case there are only two bots, the bot
	 * having the dealer button pays the small blind. When returned, 'activeSeat' is the player after the big blind.
	 */
	public void payBlinds()
	{
		// when not playing HU, the bot directly behind the dealer button gets the small blind
		activeSeat = buttonSeat;
		if(numberOfBots > 2)
			nextBotActive();			
		placeBet(sizeSB);
		// the bot behind the small blind pays the big blind
		nextBotActive();
		placeBet(sizeBB);
		lastToActSeat = activeSeat;
		nextBotActive();
	}
	
	
	/**
	 * Deals cards from the card deck to all the bots, and then passes the information to the bots.
	 */
	public void dealHandCards()
	{
		for(int i = 0; i < numberOfBots; i++)
		{
			Card card1 = deck.nextCard();
			Card card2 = deck.nextCard();
			botHands[i] = new Hand(card1, card2);
		}

		for(int i = 0; i < numberOfBots; i++)
		{
			// create an array with stack sizes excluding the stack size of the bot on seat 'i'
			int[] otherBotStacks = new int[botStacks.length - 1];
			System.arraycopy(botStacks, 0, otherBotStacks, 0, i);
			System.arraycopy(botStacks, i + 1, otherBotStacks, i, numberOfBots - i - 1);
			
			// calculate the number of positions that the bot is behind the dealer button
			int buttonOffset = i - buttonSeat;
			if(buttonOffset < 0)
				buttonOffset += numberOfBots;
			
			// give the information to the bot
			(???)[botIDs[i]].initializeHand(botHands[i].toString(), botStacks[i], otherBotStacks, buttonOffset);
		}
	}
	
	
	/**
	 * Places the next card(s) on the table after a betting round has finished. Returns the new round enum when a new
	 * round has been dealt, or false when the current round is already the last round.
	 * @param round : the round that has just been finished
	 */
	public BetRound dealNextStreet(BetRound round)
	{
		if(round == BetRound.RIVER)
			return null;
		
		round = BetRound.values()[round.ordinal() + 1];
		Card newCard = deck.nextCard();
		tableCards.add(newCard);
		if(round == BetRound.FLOP)
		{
			newCard = deck.nextCard();
			tableCards.add(newCard);
			newCard = deck.nextCard();
			tableCards.add(newCard);
		}
		return round;
	}
	
	
	/**
	 * Places a bet for the currently active bot. The stack of the bot is lowered with the bet amount and this amount
	 * is then added to the pot. If the given bet size is larger than the remaining stack size of the bot, then the bet
	 * is lowered to the remaining stack size of the bot, thereby putting him all-in. The bet size that is actually
	 * placed is returned.
	 * @param size : the size of the bet
	 */
	public int placeBet(int size)
	{
		if(botStacks[activeSeat] < size)
			size = botStacks[activeSeat];
		
		botStacks[activeSeat] -= size;
		botBetsThisRound[activeSeat] += size;
		pot.addBet(botIDs[activeSeat], size);
		return size;
	}
}
