public interface Robot
{
	public void initializeHand(TableInfo gameInfo);

	public String go(TableInfo actionInfo);
	
	public String update(TableInfo opponentInfo);
}