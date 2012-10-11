import java.util.Random;


public class TestBotRandom implements Robot
{
	
	public TestBotRandom()
	{
		
	}

	@Override
	public void initializeHand()
	{
		
	}

	@Override
	public String go()
	{
		Random r = new Random();
		int next = r.nextInt(5);
		int amount = 50*(2 + r.nextInt(4));
		if(next == 0)
			return "check,0";
		if(next == 1 || next == 2)
			return "call,0";
		else
			return "raise,"+ amount;
	}

	@Override
	public void update()
	{
		
	}
}
