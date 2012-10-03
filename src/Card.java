/**
 * A Card class object represents one card
 */
public class Card
{
	private CardHeight height;
	private CardSuit suit;
	private int number;
	
	
	/**
	 * Creates a card object using the given String. For example "6s" or "Kd"
	 */
	/*
	public Card(String cardString)
	{
		...
	}
	*/
	
	/**
	 * Creates a card object based on a number between 0 and 51
	 */
	public Card(int number)
	{
		this.number = number;
		int findSuit = number / 13;
		switch(findSuit)
		{
			case 0 : suit = CardSuit.SPADES; break;
			case 1 : suit = CardSuit.HEARTS; break;
			case 2 : suit = CardSuit.CLUBS; break;
			default : suit = CardSuit.DIAMONDS;
		}
		
		int findHeight = number % 13;
		switch(findHeight)
		{
			case 0 : height = CardHeight.DEUCE; break;
			case 1 : height = CardHeight.THREE; break;
			case 2 : height = CardHeight.FOUR; break;
			case 3 : height = CardHeight.FIVE; break;
			case 4 : height = CardHeight.SIX; break;
			case 5 : height = CardHeight.SEVEN; break;
			case 6 : height = CardHeight.EIGHT; break;
			case 7 : height = CardHeight.NINE; break;
			case 8 : height = CardHeight.TEN; break;
			case 9 : height = CardHeight.JACK; break;
			case 10 : height = CardHeight.QUEEN; break;
			case 11 : height = CardHeight.KING; break;
			default : height = CardHeight.ACE;
		}
	}
	
	
	/**
	 * Returns the number of the card as a long, with a 1 on t
	 */
	public long getNumber()
	{
		int suitShift = number / 13;
		int heightShift = number % 13;
		return (1l << (16*suitShift + heightShift));
	}
	
	
	/**
	 * Returns the height of this card
	 */
	public CardHeight getHeight()
	{
		return height;
	}
	
	
	/**
	 * Returns the suit of this card
	 */
	public CardSuit getSuit()
	{
		return suit;
	}
}
