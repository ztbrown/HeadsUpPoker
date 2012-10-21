package nl.starapple.poker;

public interface Robot {

	public void setup(long timeOut);
	
	public void writeMove(PokerMove move);
	
	public PokerMove getMove(long timeOut);

	public void writeInfo(HandInfo info);

	public void writeInfo(MatchInfo info);
	
	void writeResult(HandResultInfo info);
}
