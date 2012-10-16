package nl.starapple.poker;


public class TestBotCheckCaller implements Robot
{
	
	public TestBotCheckCaller()
	{
		
	}

	@Override
	public void writeInfo(MatchInfo info)
	{
		
	}

	@Override
	public PokerMove getMove(long timeOut)
	{
		return new PokerMove("call", 0);
	}

	@Override
	public void writeMove(PokerMove move)
	{
		
	}
}
