package nl.starapple.poker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import com.stevebrecher.HandEval;

/**
 * Class that is the engine for playing a game of poker at one table. It regulates all the actions and information
 * needed for playing a match, including the communication with the involved bots.
 */
public class MatchPlayer
{
	private String gameType;
	private int handNumber;
	private int blindLevel;
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
	private int sizeSB, sizeBB;
	private int sizeMinimalRaise;
	private int sizeCurrentRaise;
	private boolean[] isInvolvedInHand;
	private boolean[] isInvolvedInMatch;
	private boolean isTournament;
	private int[] botBetsThisRound;
	private int[] botGainLoss;
	
	private final int SIZE_STARTSTACK = 1500;
	private final int[] BLINDLEVELHEIGHTS = {20, 40, 60, 100, 150, 200, 300, 400, 600, 800, 1000, 1500, 2000, 3000};
	private final int HANDS_PER_BLINDLEVEL = 10;
	private final long SETUP_TIME = 10000l;
	private final long TIME_PER_MOVE = 500l;
	private final long TIMEBANK_MAX = 5000l;
	
	
	/**
	* Setup a table with the given bots, so that a match can be played.
	* @param botID : an array of bot IDs that are on the table
	* @param stack : the stack size that all bots start with
	* @param BB : the size of the big blind
	* @param SB : the size of the small blind
	* @param tournamentMode : whether it is a tournament or not
	*/
	public MatchPlayer(Collection<PokerBot> botList, String typeOfGame, boolean tournamentMode)
	{
		gameType = typeOfGame;
		handNumber = 0;
		blindLevel = 0;
		bots = new ArrayList<PokerBot>(botList);
		numberOfBots = botList.size();
		botStacks = new int[numberOfBots];
		botTimeBanks = new long[numberOfBots];
		for(int i = 0; i < numberOfBots; i++)
		{
			botStacks[i] = SIZE_STARTSTACK;
			botTimeBanks[i] = TIMEBANK_MAX;
		}
		
		sizeBB = BLINDLEVELHEIGHTS[0];
		sizeSB = sizeBB / 2;
		pot = new Pot(bots);
		deck = new Deck();
		tableCards = new Vector<Card>();
		botHands = new Hand[numberOfBots];
		handHistory = "";
		Random random = new Random();
		buttonSeat = random.nextInt(numberOfBots);
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
		handHistory += "Settings gameType " + gameType;
		handHistory += "\nSettings timeBank " + TIMEBANK_MAX;
		handHistory += "\nSettings timeTurn " + TIME_PER_MOVE;
		handHistory += "\nSettings players " + numberOfBots;
		for(int i = 0; i < numberOfBots; i++)
			handHistory += String.format("\nSettings seat%d %s", i, bots.get(i).getName());
		
		sendMatchInfo();
		
		while(botStacks[0] > 0 && botStacks[1] > 0)
		{
			if(isTournament && handNumber == (blindLevel + 1) * HANDS_PER_BLINDLEVEL &&
			   sizeBB < BLINDLEVELHEIGHTS[BLINDLEVELHEIGHTS.length - 1])
			{
				blindLevel++;
				sizeBB = BLINDLEVELHEIGHTS[blindLevel];
				sizeSB = sizeBB / 2;
			}
			playHand();
			writeHistory();
		}
	
        if( botStacks[0] > botStacks[1] )
        	return new int[] { 2, 0 };
        if( botStacks[0] < botStacks[1] )
        	return new int[] { 0, 2 };
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
		pot = new Pot(bots);
		round = BetRound.PREFLOP;
		
		buttonSeat += 1;
		if(buttonSeat + 1 > numberOfBots)
			buttonSeat = 0;
		
		if(!isTournament)
		{
			for(int i = 0; i < numberOfBots; i++)
			{
				botGainLoss[i] += botStacks[i] - SIZE_STARTSTACK;
				botStacks[i] = SIZE_STARTSTACK;
				isInvolvedInMatch[i] = true;
			}
		}
		
		for(int i = 0; i < numberOfBots; i++)
			if(isInvolvedInMatch[i])
				isInvolvedInHand[i] = true;
		
		//System.out.println("Stack bot 0: " + botStacks[0]);
		//System.out.println("Stack bot 1: " + botStacks[1]);
		sendHandInfo(HandInfoType.HAND_START);
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
			int amountToCall = sizeCurrentRaise - botBetsThisRound[activeSeat];
			// Prevent case that SB preflop calls all-in and BB still having to make a move.
			if(amountToCall == 0 && botStacks[1 - activeSeat] == 0)
				return;
			
			sendHandInfo(HandInfoType.PREMOVE_INFO);
			long startTime = System.currentTimeMillis();
			PokerMove nextMove = (new BotAction()).getMove(bots.get(activeSeat).getBot(), botTimeBanks[activeSeat]);
			long timeElapsed = System.currentTimeMillis() - startTime;
			
			// update the timebank of the current bot with the elapsed time and increment it for the next move
			botTimeBanks[activeSeat] = Math.max(botTimeBanks[activeSeat] - timeElapsed, 0);
			botTimeBanks[activeSeat] = Math.min(botTimeBanks[activeSeat] + TIME_PER_MOVE, TIMEBANK_MAX);
			
			if(nextMove == null)
			{
				nextMove = new PokerMove("check", 0);
				System.err.println("bot_" + activeSeat + " did not act in time, action set to \"check\"");
			}
		
			
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
			// handHistory += String.format("\n%s %s %d", bots.get(activeSeat).getName(), botAction, botActionAmount);
			handHistory += String.format("\n%s %s %d", bots.get(activeSeat).getName(), botAction, botBetsThisRound[activeSeat]);
			//System.out.println("|_| Total pot size: " + pot.getCurrentPotSize());
			sendMoveInfo(botAction, botActionAmount);
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
		sendHandInfo(HandInfoType.HAND_CARDS);
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
			
		sendHandInfo(HandInfoType.NEW_BETROUND);
		//System.out.println(table + "]");
		if(!handHistory.endsWith("]"))
		{
			ArrayList<Integer> allPots = pot.getPots(botsInvolvedToArrayList());			
			handHistory += String.format("\nMatch pot %d", allPots.get(0));
			for(int i = 1; i < allPots.size(); i++)
				handHistory += String.format("\nMatch sidepot%d %d", i, allPots.get(i));
		}
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
		handHistory += String.format("\n%s post %s", bots.get(activeSeat).getName(), botBetsThisRound[activeSeat]);
		sendMoveInfo("post", botBetsThisRound[activeSeat]);
		
		// the bot behind the small blind pays the big blind
		nextBotActive();
		botBetsThisRound[activeSeat] = placeBet(sizeBB);
		//System.out.println("Bot " + activeSeat + " pays BB of " + botBetsThisRound[activeSeat]);
		handHistory += String.format("\n%s post %s", bots.get(activeSeat).getName(), botBetsThisRound[activeSeat]);
		sendMoveInfo("post", botBetsThisRound[activeSeat]);
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
		HashMap<PokerBot, Integer> botHandStrengths = new HashMap<PokerBot, Integer>();			
		long tableCardsCode = 0l;
		long playerCombinationCode = 0l;
		for(int i = 0; i < tableCards.size(); i++)
			tableCardsCode += tableCards.get(i).getNumber();
		
		for(int i = 0; i < numberOfBots; i++)
		{
			if(isInvolvedInHand[i])
			{
				playerCombinationCode = tableCardsCode;
				playerCombinationCode += botHands[i].getCard1().getNumber();
				playerCombinationCode += botHands[i].getCard2().getNumber();
				botHandStrengths.put(bots.get(i), HandEval.hand7Eval(playerCombinationCode));
			}
		}
		int numberOfBotsOnShowdown = botHandStrengths.size();
			
		Pot.PayoutWinnerInfo winnerInfo = pot.payoutWinners(botHandStrengths);
		ArrayList<Integer> potParts = winnerInfo.getPots();
		ArrayList<ArrayList<PokerBot>> potPartWinners = winnerInfo.getWinnerPerPot();
		
		int[] winPerBot = new int[numberOfBots];
		for(int i = potParts.size() - 1; i >= 0; i--)
		{
			ArrayList<PokerBot> currentPotWinners = potPartWinners.get(i);
			int currentPotSize = potParts.get(i);
			int numberOfWinners = currentPotWinners.size();
			int amountPerWinner = currentPotSize / numberOfWinners;
			int restChips = currentPotSize - (numberOfWinners*amountPerWinner);
			int currentSeat = (buttonSeat + 1) % numberOfBots;
			
			String potWinnersStr = "[";
			while(true)
			{
				PokerBot currentBot = bots.get(currentSeat);
				if(currentPotWinners.contains(currentBot))
				{
					int currentWinAmount = amountPerWinner;
					if(restChips-- > 0)
						currentWinAmount++;
					
					potWinnersStr += String.format("%s:%d,", currentBot.getName(), currentWinAmount);
					winPerBot[currentSeat] += currentWinAmount;
				}
				currentSeat = (currentSeat + 1) % numberOfBots;
				if(currentSeat == (buttonSeat + 1) % numberOfBots)
					break;
			}
			potWinnersStr = potWinnersStr.substring(0, potWinnersStr.length() - 1);
			potWinnersStr += "]";

			if(i > 0)
				handHistory += String.format("\nResult sidepot%d %s", i, potWinnersStr);
			else
				handHistory += String.format("\nResult pot %s", potWinnersStr);			
		}

		for(int i = 0; i < numberOfBots; i++)
		{
			botStacks[i] += winPerBot[i];
		}
		sendResultInfo(winPerBot, numberOfBotsOnShowdown > 1);
	}
	
	
	/**
	 * Returns an ArrayList of all bots that are marked as involved.
	 */
	private ArrayList<PokerBot> botsInvolvedToArrayList()
	{
		ArrayList<PokerBot> botsInvolved = new ArrayList<PokerBot>();
		for(int i = 0; i < isInvolvedInHand.length; i++)
			if(isInvolvedInHand[i])
				botsInvolved.add(bots.get(i));		
		return botsInvolved;
	}
	
	
	/**
	 * Sends the match info to all the bots that are playing at this table. Gives the bots some time to prepare for 
	 * playing a match, the method waits for all bots to return from setup for a maximum time of 'SETUP_TIME'. This
	 * method should be called at the start of a new match.
	 */
	private void sendMatchInfo()
	{
		boolean botsReady[] = new boolean[numberOfBots];
		MatchInfo info = new MatchInfo(bots, gameType, isTournament, TIMEBANK_MAX, TIME_PER_MOVE,
									   HANDS_PER_BLINDLEVEL);		
		for(int i = 0; i < numberOfBots; i++)
		{
			info.setCurrentBotInfo(i);
			bots.get(i).getBot().writeInfo(info);
			botsReady[i] = false;
		}		
		
		Thread[] setupThreads = new Thread[numberOfBots];
		for(int i = 0; i < numberOfBots; i++)
		{
			final Robot bot = bots.get(i).getBot();
			setupThreads[i] = new Thread()
			{
		        public void run()
		        {
		        	bot.setup(SETUP_TIME);
		        }
			};
		}
		
		for(int i = 0; i < numberOfBots; i++)
		{
			setupThreads[i].start();
			try{ setupThreads[i].join(); }
				catch(InterruptedException e){ e.printStackTrace(); }
		}
	}
	
	
	/**
	 * Sends the hand info to all the bots that are playing at this table. This method should be called at the start
	 * of each new round and at each new bet round.
	 */
	private void sendHandInfo(HandInfoType type)
	{
		ArrayList<Integer> allPots = pot.getPots(botsInvolvedToArrayList());
		HandInfo info = new HandInfo(type, handNumber, bots, botStacks, sizeBB, sizeSB, buttonSeat,
									   tableCards.toString().replaceAll("\\s", ""), allPots);
		// The pre-move info only goes to the active bot.
		if(type.equals(HandInfoType.PREMOVE_INFO) )
		{		
			bots.get(activeSeat).getBot().writeInfo(info);
			return;
		}
		for(int i = 0; i < numberOfBots; i++)
		{
			info.setCurrentBotInfo(i, botHands[i]);
			bots.get(i).getBot().writeInfo(info);
		}
	}
	
	
	/**
	 * Sends a message to all bots about the move that a bot made.
	 * @param action : the action that the bot made
	 * @param amount : the amount belonging to the action
	 */
	private void sendMoveInfo(String action, int amount)
	{
		PokerMove move = new PokerMove(action, amount);
		move.setPlayer("bot_" + activeSeat);
		for(int i = 0; i < numberOfBots; i++)
			bots.get(i).getBot().writeMove(move);
	}
	
	
	/**
	 * Sends a message to all bots about the result of the hand, thus how the pot is distributed, and in case of a
	 * showdown it also provides the hands of the winners.
	 * @param potDivision : array with the amount of the pot that each bot gets
	 * @param showdown : whether there is a showdown or not
	 */
	private void sendResultInfo(int[] potDivision, boolean showdown)
	{
		HandResultInfo resultInfo = new HandResultInfo(bots, potDivision);
		if(showdown)
		{
			for(int i = 0; i < numberOfBots; i++)
				if(isInvolvedInHand[i])
					resultInfo.setBotHand(i, botHands[i]);
		}
		for(int i = 0; i < numberOfBots; i++)
			bots.get(i).getBot().writeResult(resultInfo);
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