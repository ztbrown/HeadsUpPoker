package nl.starapple.poker;
import java.util.ArrayList;
import java.util.Collection;

public class EngineTester {

	public static void main(String[] args)
	{		
		PokerBot pbot1 = new PokerBot(new TestBotRandom(), "Bot_1", "Henkie");
		PokerBot pbot2 = new PokerBot(new TestBotRandom(), "Bot_2", "Japie");
		Collection<PokerBot> bots = new ArrayList<PokerBot>();
		bots.add(pbot1);
		bots.add(pbot2);
		MatchPlayer engine = new MatchPlayer(bots, "NLH", true);
		engine.runMatch();
		
		
		/* //Testing for method getPots() from class Pot
		PokerBot pbot3 = new PokerBot(new TestBotRandom(), "Bot_3", "Henkie");
		PokerBot pbot4 = new PokerBot(new TestBotRandom(), "Bot_4", "Japie");
		bots.add(pbot3);
		bots.add(pbot4);
		Pot pot = new Pot(bots);
		pot.addBet(pbot1, 100);
		pot.addBet(pbot2, 800);
		pot.addBet(pbot3, 500);
		pot.addBet(pbot4, 400);
		
		ArrayList<PokerBot> involvedBots = new ArrayList<PokerBot>();
		involvedBots.add(pbot1);
		involvedBots.add(pbot2);
		//involvedBots.add(pbot3);
		//involvedBots.add(pbot4);
		ArrayList<Integer> potParts = pot.getPots(involvedBots);
		
		for(int i = 0; i < potParts.size(); i++)
			System.out.println("Match pot " + potParts.get(i));
		*/
	}
}
