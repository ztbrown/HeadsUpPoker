package nl.starapple.poker;


public class TestBotCheckFolder implements Robot
{
	
	public TestBotCheckFolder()
	{
		
	}

	@Override
	public void writeInfo(MatchInfo info)
	{
		
	}

	@Override
	public PokerMove getMove(long timeOut)
	{
		return new PokerMove("check", 0);
	}

	@Override
	public void writeMove(PokerMove move)
	{
		
	}
}
