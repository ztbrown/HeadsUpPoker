package nl.starapple.poker;


public class TestBotRaiser implements Robot
{
	
	public TestBotRaiser()
	{
		
	}

	@Override
	public void writeInfo(MatchInfo info)
	{
		
	}

	@Override
	public PokerMove getMove(long timeOut)
	{
		return new PokerMove("raise", 100);
	}

	@Override
	public void writeMove(PokerMove move)
	{
		
	}
}
