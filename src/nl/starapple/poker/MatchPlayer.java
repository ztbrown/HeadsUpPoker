package nl.starapple.poker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import com.stevebrecher.HandEval;

/**
 * Class that is the engine for playing a game of poker at one table. It regulates all the actions and information
 * needed for playing a match, including the communication with the involved bots.
 */
public class MatchPlayer {

	private int handNumber;
	private ArrayList<PokerBot> bots;
	private int numberOfBots;
	private Deck deck;
	private Pot pot;
	private Vector<Card> tableCards;
	private BetRound round;
	private Hand[] botHands;
	private String handHistory;
	private int[] botStacks;
	private long[] botTimeBanks;
	private int buttonSeat;
	private int activeSeat;
	private int lastToActSeat;
	private int sizeStartStack;
	private int sizeSB, sizeBB;
	private int sizeMinimalRaise;
	private int sizeCurrentRaise;
	private boolean[] isInvolvedInHand;
	private boolean[] isInvolvedInMatch;
	private boolean isTournament;
	private int[] botBetsThisRound;
	private int[] botGainLoss;
	
	private final long TIME_PER_MOVE = 1000l;
	private final long TIMEBANK_MAX = 10000l;
	
	
	/**
	* Setup a table with the given bots, so that a match can be played.
	* @param botID : an array of bot IDs that are on the table
	* @param stack : the stack size that all bots start with
	* @param BB : the size of the big blind
	* @param SB : the size of the small blind
	* @param tournamentMode : whether it is a tournament or not
	*/
	public MatchPlayer(Collection<PokerBot> botList, int stack, int BB, int SB, boolean tournamentMode)
	{
		handNumber = 0;
		bots = new ArrayList<PokerBot>(botList);
		numberOfBots = botList.size();
		sizeStartStack = stack;
		botStacks = new int[numberOfBots];
		botTimeBanks = new long[numberOfBots];
		for(int i = 0; i < numberOfBots; i++)
		{
			botStacks[i] = sizeStartStack;
			botTimeBanks[i] = TIMEBANK_MAX;
		}
		
		sizeBB = BB;
		sizeSB = SB;
		pot = new Pot(bots);
		deck = new Deck();
		tableCards = new Vector<Card>();
		botHands = new Hand[numberOfBots];
		handHistory = "";
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
		botBetsThisRound = new int[numberOfBots];
		if(!isTournament)
			botGainLoss = new int[numberOfBots];
	}
	
	
	/**
	 * Starts the match. Plays hands until one of the bots has no chips left.
	 */
	public int[] runMatch()
	{
		int maxHands = 50;
		int handsPlayed = 0;

		handHistory += "Settings gameType NLH";
		handHistory += "\nSettings timeBank " + TIMEBANK_MAX;
		handHistory += "\nSettings timeTurn " + TIME_PER_MOVE;
		handHistory += "\nSettings players " + numberOfBots;
		for(int i = 0; i < numberOfBots; i++)
			handHistory += String.format("\nSettings seat%d %s", i, bots.get(i).getName());
		while(botStacks[0] > 0 && botStacks[1] > 0)
		{
			playHand();
			writeHistory();
			if(++handsPlayed >= maxHands )
				break;
		}
	
        /*
        if( botStacks[0] == 0 ) { return new int[] { 2, 1 }; }
        if( botStacks[1] == 0 ) { return new int[] { 1, 2 }; }
        return new int[] { 1, 1 };
        */
        if( botStacks[0] > botStacks[1] )
        	return new int[] { 0, 2 };
        if( botStacks[0] < botStacks[1] )
        	return new int[] { 2, 0 };
        return new int[] { 1, 1 };
	}
	
	
	/**
	* Play the next hand of the match.
	*/
	private void playHand()
	{
		setupNextHand();
		payBlinds();
		dealHandCards();
				
		boolean bettingFinished = false;
		while(!bettingFinished)
		{	
			boolean roundFinished = false;
			if(!setupBetRound())
				roundFinished = true;
			while(!roundFinished)
			{
				nextBotAction();			
				// if no one has to act anymore, the bet round is over
				if(lastToActSeat == activeSeat || !isInvolvedInHand[0] || !isInvolvedInHand[1])
					roundFinished = true;
				else
					nextBotActive();
			}
			
			if(isInvolvedInHand[0] && isInvolvedInHand[1])
			{
				if(!dealNextStreet())
					bettingFinished = true;
			}
			else
				bettingFinished = true;
		}
		
		distributePot();	
	}
	
	
	/**
	* Prepares for playing the next hand, the dealer button is moved to the next bot and all bots are reset to being
	* involved in the next hand. If the game is not a tournament then the gain or loss per bot on the last hand is
	* stored and all stack sizes are reset to the standard starting stack and all bots are reset to being involved in
	* the match.
	*/
	private void setupNextHand()
	{
		handNumber++;
		tableCards = new Vector<Card>();
		deck.resetDeck();
		round = BetRound.PREFLOP;
		
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
		
		//System.out.println("Stack bot 0: " + botStacks[0]);
		//System.out.println("Stack bot 1: " + botStacks[1]);
		handHistory += String.format("\nMatch hand %d", handNumber);
		handHistory += String.format("\n%s stack %d", bots.get(0).getName(), botStacks[0]);
		handHistory += String.format("\n%s stack %d", bots.get(1).getName(), botStacks[1]);
	}
	
	
	/**
	 * Prepares for playing the next betting round of the current hand. This involves setting the bot that is next to
	 * act and that is last to act. It checks whether more actions are needed, which depends on whether one of the two
	 * bots is all-in by paying the blinds or when a bot went all-in later on. Returns a boolean telling whether more
	 * bot actions are needed or not.
	 */
	private boolean setupBetRound()
	{	
		int numberOfBotsActiveInHand = 0;
		for(int i = 0; i < numberOfBots; i++)
			if(isInvolvedInHand[i] && botStacks[i] > 0)
				numberOfBotsActiveInHand++;
		
		if(round.equals(BetRound.PREFLOP))
		{
			activeSeat = buttonSeat;
			lastToActSeat = 1 - buttonSeat;
			sizeCurrentRaise = Math.max(botBetsThisRound[0], botBetsThisRound[1]);
			
			if(numberOfBotsActiveInHand == 0)
				return false;
			else if(numberOfBotsActiveInHand == 1)
			{
				// if after paying the blinds the small blind is all-in or the big blind is all-in with an amount less
				// than a small blind, then there are no further actions
				if(botStacks[activeSeat] == 0 ||
				  (botStacks[1 - activeSeat] == 0 && botBetsThisRound[1 - activeSeat] <= botBetsThisRound[activeSeat]))
					return false;
				// if the big blind is all-in by paying his blind and it is larger than the small blind, then the small
				// blind is the last bot to act
				else
					lastToActSeat = buttonSeat;
			}		
		}
		else
		{
			activeSeat = 1 - buttonSeat;
			lastToActSeat = buttonSeat;
			sizeCurrentRaise = 0;
			botBetsThisRound = new int[numberOfBots];
			
			// if there are less than two bots that can make actions, then there are no further actions
			if(numberOfBotsActiveInHand < 2)
				return false;		
		}
		
		sizeMinimalRaise = sizeBB;
		return true;
	}
	
	
	/**
	* Used to increment 'activeSeat' so that it becomes the position of the next bot.
	*/
	private void nextBotActive()
	{
		activeSeat += 1;
		if(activeSeat + 1 > numberOfBots)
			activeSeat = 0;
	}
	
	
	/**
	 * Asks the currently active bot to act, provided that it has a non-zero stack and is still involved in the current
	 * hand. Checks and possibly alters the received action according to the playing rules. Updates the state of the
	 * hand according to the chosen action.
	 */
	private void nextBotAction()
	{
		if(botStacks[activeSeat] > 0 && isInvolvedInHand[activeSeat])
		{
			long startTime = System.currentTimeMillis();
			PokerMove nextMove = (new BotAction()).getMove(bots.get(activeSeat).getBot(), botTimeBanks[activeSeat]);
			long timeElapsed = System.currentTimeMillis() - startTime;
			
			// update the timebank of the current bot with the elapsed time and increment it for the next move
			botTimeBanks[activeSeat] = Math.max(botTimeBanks[activeSeat] - timeElapsed, 0);
			botTimeBanks[activeSeat] = Math.min(botTimeBanks[activeSeat] + TIME_PER_MOVE, TIMEBANK_MAX);
			
			if(nextMove == null)
			{
				nextMove = new PokerMove("check", 0);
				System.err.println(bots.get(activeSeat).getName() + " did not act in time, action set to \"check\"");
			}
		
			int amountToCall = sizeCurrentRaise - botBetsThisRound[activeSeat];
			String botAction = nextMove.getAction();
			int botActionAmount = nextMove.getAmount();
			
			// Handle invalid / unlogical actions
			if(botStacks[1 - activeSeat] == 0 && botAction.equals("raise"))
			{
				botAction = "call";
				System.err.println("Other player is already all-in, raise action changed to \"call\"");
			}
			else if(botAction.equals("call") && amountToCall == 0)
			{
				botAction = "check";
				System.err.println("Other player did not bet, call action changed to \"check\"");
			}
			else if(botAction.equals("check") && amountToCall > 0)
			{
				botAction = "fold";
				isInvolvedInHand[activeSeat] = false;
				System.err.println("Other player did make a bet, check action changed to \"fold\"");
			}
			
			// Handle the actual action that is being made
			if(botAction.equals("raise"))
			{							
				// if the chosen raise size is too small, increase it to the minimum amount
				if(botActionAmount < sizeMinimalRaise)
				{
					botActionAmount = sizeMinimalRaise;
					if(botStacks[activeSeat] > amountToCall + botActionAmount)
						System.err.println("Raise is below minimum amount, automatically changed to minimum");								
				}							
				int realBetAmount = placeBet(amountToCall + botActionAmount);
				
				if(realBetAmount <= amountToCall)
				{
					botAction = "call";
					botActionAmount = realBetAmount;
					System.err.println("Raise is below amount to call, action automatically changed to \"call\"");
					//System.out.println("Bot " + activeSeat + ": action: " + botAction + " " + realBetAmount);
				}
				else
				{
					updateLastSeatToAct();
					sizeCurrentRaise = botBetsThisRound[activeSeat];
					botActionAmount = realBetAmount - amountToCall;
					// update the minimal allowed raise size based on this new larger raise
					if(botActionAmount > sizeMinimalRaise)
						sizeMinimalRaise = realBetAmount - amountToCall;
					
					//System.out.println("Bot " + activeSeat + ": action: " + botAction + " " + botActionAmount + ", to " + botBetsThisRound[activeSeat] + " total");
				}
			}
			else if(botAction.equals("call"))
			{
				botActionAmount = placeBet(amountToCall);
				//System.out.println("Bot " + activeSeat + ": action: " + botAction + " " + botActionAmount);
			}
			else if(botAction.equals("check"))
			{
				botActionAmount = 0;
				//System.out.println("Bot " + activeSeat + ": action: " + botAction);
			}
			// the default action is fold
			else
			{
				botActionAmount = 0;
				isInvolvedInHand[activeSeat] = false;
				//System.out.println("Turn: " + activeSeat + ", action: " + botAction);
			}
			
			// send a message to all other bots about the action
			handHistory += String.format("\n%s %s %d", bots.get(activeSeat).getName(), botAction, botActionAmount);
			//System.out.println("|_| Total pot size: " + pot.getCurrentPotSize());
			PokerMove move = new PokerMove(botAction, botActionAmount);
			move.setPlayer(bots.get(activeSeat).getName());
			for(int i = 0; i < numberOfBots; i++)
				bots.get(i).getBot().writeMove(move);
		}
	}
	
	
	/**
	* Updates which seat is the last seat that must act in the current round
	*/
	private void updateLastSeatToAct()
	{
		lastToActSeat = activeSeat - 1;
		if(lastToActSeat < 0)
			lastToActSeat = numberOfBots - 1;
	}
	
	
	/**
	* Deals cards from the card deck to all the bots, and then passes the information to the bots.
	*/
	private void dealHandCards()
	{
		for(int i = 0; i < numberOfBots; i++)
		{
			Card card1 = deck.nextCard();
			Card card2 = deck.nextCard();
			botHands[i] = new Hand(card1, card2);
			handHistory += String.format("\n%s hand %s", bots.get(i).getName(), botHands[i].toString());
			//System.out.println("Bot " + i + " hand: " + botHands[i].toString());
		}
		sendMatchInfo();
	}
	
	
	/**
	* Places the next card(s) on the table. Returns false if we are on the river already, returns true otherwise.
	*/
	private boolean dealNextStreet()
	{
		if(round == BetRound.RIVER)
			return false;
		
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
		
		String table = "[" + tableCards.get(0).toString();
		for(int i = 1; i < tableCards.size(); i++)
			table += "," + tableCards.get(i).toString();
		table += "]";
			
		sendMatchInfo();
		//System.out.println(table + "]");
		if(!handHistory.endsWith("]"))
			handHistory += "\nMatch pot " + pot.getCurrentPotSize();
		handHistory += "\nMatch table " + table;
		
		return true;
	}
	
	
	/**
	* Forces the next two players after the dealer button to pay the blinds. In case there are only two bots, the bot
	* having the dealer button pays the small blind. When returned, 'activeSeat' is the player with the big blind.
	*/
	private void payBlinds()
	{
		// when not playing HU, the bot directly behind the dealer button gets the small blind
		activeSeat = buttonSeat;
		if(numberOfBots > 2)
			nextBotActive();
		botBetsThisRound[activeSeat] = placeBet(sizeSB);		
		//System.out.println("Bot " + activeSeat + " pays SB of " + botBetsThisRound[activeSeat]);
		handHistory += String.format("\n%s sb %s", bots.get(activeSeat).getName(), botBetsThisRound[activeSeat]);
		
		// the bot behind the small blind pays the big blind
		nextBotActive();
		botBetsThisRound[activeSeat] = placeBet(sizeBB);
		//System.out.println("Bot " + activeSeat + " pays BB of " + botBetsThisRound[activeSeat]);
		handHistory += String.format("\n%s bb %s", bots.get(activeSeat).getName(), botBetsThisRound[activeSeat]);
	}
	
	
	/**
	* Places a bet for the currently active bot. The stack of the bot is lowered with the bet amount and this amount
	* is then added to the pot. If the given bet size is larger than the remaining stack size of the bot, then the bet
	* is lowered to the remaining stack size of the bot, thereby putting him all-in. The bet size that is actually
	* placed is returned.
	* @param size : the size of the bet
	*/
	private int placeBet(int size)
	{
		if(botStacks[activeSeat] < size)
			size = botStacks[activeSeat];
		
		botStacks[activeSeat] -= size;
		botBetsThisRound[activeSeat] += size;
		pot.addBet(bots.get(activeSeat), size);
		return size;
	}

	
	/**
	 * Checks if there is a showdown or not. When there is a showdown, the winner is determined. Then the amount of the
	 * pot the winner has right to is given to the winner, a potential side pot is given back to the bot that put it
	 * in.
	 */
	private void distributePot()
	{
		int[] botPots = {0,0};
		
		// There is a showdown if both bots are still in the hand. Calculate who wins
		if(isInvolvedInHand[0] && isInvolvedInHand[1])
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
			// case of a tie
			if(botHandStrength[0] == botHandStrength[1])
			{
				botPots = pot.getTwoWinnersPot(bots.get(0), bots.get(1));
				//System.out.println("> result: it's a tie");
			}
			// case of one winner
			else
			{
				int winnerSeat = 0;
				if(botHandStrength[1] > botHandStrength[0])
					winnerSeat = 1;
				
				botPots[winnerSeat] = pot.getWinnersPot(bots.get(winnerSeat), 0);				
				if(!pot.isEmpty())
				{
					//System.out.println("> result: bot " + (1 - winnerSeat) + " gets the side pot returned");
					botPots[1 - winnerSeat] = pot.getWinnersPot(bots.get(1 - winnerSeat), 0);
				}
				//System.out.println("> result: bot " + winnerSeat + " wins the pot");
			}
			
			
		}
		// No showdown, one of the bots folded, so the other bot gets the whole pot
		else
		{
			int winnerSeat = 0;
			if(!isInvolvedInHand[0])
				winnerSeat = 1;

			botPots[winnerSeat] = pot.getWinnersPot(bots.get(winnerSeat), 0);
			//System.out.println("> result: bot " + winnerSeat + " wins the pot");
		}
		
		botStacks[0] += botPots[0];
		botStacks[1] += botPots[1];
		if(botPots[0] > 0)
			handHistory += String.format("\n%s wins %d", bots.get(0).getName(), botPots[0]);
		if(botPots[1] > 0)
			handHistory += String.format("\n%s wins %d", bots.get(1).getName(), botPots[1]);
	}
	
	
	/**
	 * Sends the match info to all the bots that are playing at this table. This method should be called at the start
	 * of each new round and at each new bet round.
	 */
	private void sendMatchInfo()
	{
		MatchInfo info = new MatchInfo(handNumber, bots, botStacks, sizeBB, sizeSB, buttonSeat,
									   tableCards.toString().replaceAll("\\s", ""), pot.getCurrentPotSize());
		for(int i = 0; i < numberOfBots; i++)
		{
			info.setCurrentBotInfo(bots.get(i), botHands[i].toString());
			bots.get(i).getBot().writeInfo(info);
		}
	}
	
	
	/**
	 * Writes the history that is currently stored in 'handHistory' to the standard out channel and empties the String
	 * afterwards. Should be called after each finished hand.
	 */
	private void writeHistory()
	{
		System.out.println(handHistory);
		handHistory = "";
	}
}