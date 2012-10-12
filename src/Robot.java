
public interface Robot {

	public void writeMove(PokerMove move);
	
	public PokerMove getMove(long timeOut);

	public void writeInfo(MatchInfo info);
	
}
