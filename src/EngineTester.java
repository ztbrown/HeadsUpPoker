import java.util.ArrayList;
import java.util.Collection;

public class EngineTester {

	public static void main(String[] args)
	{
		Robot bot1 = new TestBotRandom();
		Robot bot2 = new TestBotRandom();
		Collection<Robot> bots = new ArrayList<Robot>();
		bots.add(bot2);
		bots.add(bot1);
		MatchPlayer engine = new MatchPlayer(bots, 1500, 50, 25, true);
		engine.runMatch();
	}
}
