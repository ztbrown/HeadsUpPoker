package nl.starapple.test;

import nl.starapple.poker.*;
import java.util.Random;

public class TestBotRandom implements Robot
{
	public TestBotRandom()
	{
		
	}
	
	@Override
	public void setup(long timeOut)
	{
		//System.out.println("Setup queried");
	}

	@Override
	public void writeInfo(HandInfo info)
	{
		//System.out.println(info);
	}

	@Override
	public PokerMove getMove(long timeOut)
	{
		Random r = new Random();
		int next = r.nextInt(5);
		int amount = 50*(2 + r.nextInt(4));
		if(next == 0)
			return new PokerMove("check", 0);
		if(next == 1 || next == 2)
			return new PokerMove("call", 0);
		else
			return new PokerMove("raise", amount);
	}

	@Override
	public void writeMove(PokerMove move)
	{
		//System.out.println(move.toString());
	}
	
	@Override
	public void writeResult(HandResultInfo info)
	{
		//System.out.println(info);
	}

	@Override
	public void writeInfo(MatchInfo info)
	{
		//System.out.println(info);
	}
}
