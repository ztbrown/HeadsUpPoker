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
		MatchPlayer engine = new MatchPlayer(bots, 1500, 50, 25, true);
		engine.runMatch();
	}
}
