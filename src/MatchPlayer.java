import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import com.stevebrecher.HandEval;

public class MatchPlayer {

	private ArrayList<Robot> bots;
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
	public MatchPlayer(Collection<Robot> botList, int stack, int BB, int SB, boolean tournamentMode)
	{
		bots = new ArrayList<Robot>(botList);
		numberOfBots = botList.size();
		sizeStartStack = stack;
		botStacks = new int[numberOfBots];
		for(int i = 0; i < numberOfBots; i++)
			botStacks[i] = sizeStartStack;
		
		sizeBB = BB;
		sizeSB = SB;
		pot = new Pot(bots);
		deck = new Deck();
		tableCards = new Vector<Card>();
		botHands = new Hand[numberOfBots];
		buttonSeat = numberOfBots;
		activeSeat = 0;
		isInvolvedInHand = new boolean[numberOfBots];
		isInvolvedInMatch = new boolean[numberOfBots];
		for(int i = 0; i < numberOfBots; i++)
		{
			isInvolvedInHand[i] = true;
			isInvolvedInMatch[i] = true;
		}
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
		
		if(!isTournament)
		{
			for(int i = 0; i < numberOfBots; i++)
			{
				botGainLoss[i] += botStacks[i] - sizeStartStack;
				botStacks[i] = sizeStartStack;
				isInvolvedInMatch[i] = true;
			}
		}
		
		for(int i = 0; i < numberOfBots; i++)
			if(isInvolvedInMatch[i])
				isInvolvedInHand[i] = true;
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
		
		boolean needShowdown = true;
		boolean bettingFinished = false;
		
		// version for HU
		while(!bettingFinished)
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
			botBetsThisRound = new int[numberOfBots];
			
			boolean roundFinished = false;
			while(!roundFinished)
			{
				if(isInvolvedInHand[activeSeat] && botStacks[activeSeat] > 0)
				{
					int amountToCall = currentRaiseSize - botBetsThisRound[activeSeat];
					String botActionString = bots.get(activeSeat).go(...);
					String[] botActionSubStrings = botActionString.split(",");
					String botAction = botActionSubStrings[0];
					int botActionAmount = Integer.parseInt(botActionSubStrings[1]);
					
					if(raiseLockActive && botAction.equals("raise"))
					{
						botAction = "call";
						System.err.println("Other player is already all-in, raise action changed to \"call\"");
					}
					
					if(botAction.equals("call"))
					{
						if(amountToCall == 0)
							System.err.println("No bet to call, action automatically changed to \"check\"");
						else
						{
							int realCallSize = placeBet(amountToCall);
							if(botStacks[activeSeat] == 0)
								numberOfBotsActiveInHand--;
						}
					}
					
					else if(botAction.equals("check"))
					{
						if(amountToCall > 0)
						{
							System.err.println("Check is not an option, action automatically changed to \"fold\"");
							isInvolvedInHand[activeSeat] = false;
						}
					}
					
					else if(botAction.equals("raise"))
					{							
						// if the chosen raise size is too small, increase it to the minimum amount
						if(botActionAmount < minimalRaiseSize)
						{
							botActionAmount = minimalRaiseSize;
							if(botStacks[activeSeat] > amountToCall + botActionAmount)
								System.err.println("Raise is below minimum amount, automatically changed to minimum");								
						}							
						int realBetAmount = placeBet(amountToCall + botActionAmount);
						
						if(realBetAmount <= amountToCall)
						{
							System.err.println("Raise is below amount to call, action automatically changed to \"call\"");
							numberOfBotsActiveInHand--;
						}
						else
						{
							updateLastSeatToAct();
							currentRaiseSize = botBetsThisRound[activeSeat];
							// if the raise is smaller than a minimal raise
							if(realBetAmount < amountToCall + minimalRaiseSize)
							{
								numberOfBotsActiveInHand--;
								raiseLockActive = true;
							}
							else
							{
								minimalRaiseSize = realBetAmount - amountToCall;
								lastRaiseSeat = activeSeat;
							}								
						}
					}
					
					// the default action is fold
					else
					{
						//isInvolvedInHand[activeSeat] = false;
						//numberOfBotsActiveInHand--;
						//numberOfBotsInHand--;
						roundFinished = true;
						bettingFinished = true;
						needShowdown = false;
					}
					
					// send a message to all other bots about the action
					for(int i = 0; i < numberOfBots; i++)
						if(isInvolvedInMatch[i] && i != activeSeat)
							bots.get(i).update(...);
				}
			
				// if no one has to act anymore, the bet round is over
				if(lastToActSeat == activeSeat)
					roundFinished = true;
				else
					nextBotActive();
			}
			if(!needShowdown || dealNextStreet(round) == BetRound.PREFLOP)
				bettingFinished = true;
		}
		
		// version for more than 2 players
		/*
		while(!handFinished)
		{
			// if there is only one active bot left in the hand, but one or more others are all-in, then deal all
			// the table cards, there is then no action in further rounds, but there has to come a showdown
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
						String botActionString = bots.get(activeSeat).go();
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
			if(dealNextStreet(round) == BetRound.PREFLOP)
				handFinished = true;
		}
		*/
		
		// Calculate who wins the showdown
		if(needShowdown)
		{
			int[] botHandStrength = new int[numberOfBots];
			long tableCardsCode = 0l;
			long playerCombinationCode = 0l;
			for(int i = 0; i < tableCards.size(); i++)
				tableCardsCode += tableCards.get(i).getNumber();
			
			for(int i = 0; i < numberOfBots; i++)
			{
				playerCombinationCode = tableCardsCode;
				playerCombinationCode += botHands[i].getCard1().getNumber();
				playerCombinationCode += botHands[i].getCard2().getNumber();
				botHandStrength[i] = HandEval.hand7Eval(playerCombinationCode);
			}
			
			if(botHandStrength[0] == botHandStrength[1])
			{
				int[] botPots = pot.getTwoWinnersPot(bots.get(0), bots.get(1));
				botStacks[0] += botPots[0];
				botStacks[1] += botPots[1];
			}
			else
			{
				int winnerSeat = 0;
				if(botHandStrength[1] > botHandStrength[0])
					winnerSeat = 1;
				
				botStacks[winnerSeat] += pot.getWinnersPot(bots.get(winnerSeat), 0);
			}
		}
		// No showdown, one of the bots folded, so the other bot gets the whole pot
		else
		{
			int winnerSeat = 0;
			if(!isInvolvedInHand[0])
				winnerSeat = 1;

			botStacks[winnerSeat] += pot.getWinnersPot(bots.get(winnerSeat), 0);
		}
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
	* Updates which seat is the last seat that must act in the current round
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
			bots.get(i).initializeHand(...);
	}
	
	
	/**
	* Places the next card(s) on the table after a betting round has finished. Returns the new round enum when a new
	* round has been dealt, or false when the current round is already the last round.
	* @param round : the round that has just been finished
	*/
	public BetRound dealNextStreet(BetRound round)
	{
		// non-elegant solution: returns 'preflop' as next betroun, the caller then knows that there's no new betround
		if(round == BetRound.RIVER)
			return BetRound.PREFLOP;
		
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
		pot.addBet(bots.get(activeSeat), size);
		return size;
	}
}