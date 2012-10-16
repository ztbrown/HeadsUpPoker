package nl.starapple.poker;


public class TestBotRaiser implements Robot
{
	
	public TestBotRaiser()
	{
		
	}

	@Override
	public void writeInfo(HandInfo info)
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

	@Override
	public void writeResult(HandResultInfo info)
	{
		
	}

	@Override
	public void writeInfo(MatchInfo info)
	{
		
	}
}
