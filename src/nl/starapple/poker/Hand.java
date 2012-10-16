package nl.starapple.poker;
public class Hand
{
	private Card card1, card2;
	
	
	/**
	 * A hand containing two cards
	 * @param firstCard : the first card
	 * @param secondCard : the second card
	 */
	public Hand(Card firstCard, Card secondCard)
	{
		card1 = firstCard;
		card2 = secondCard;
	}
	
	
	/**
	* Returns the first card of this hand
	*/
	public Card getCard1()
	{
		return card1;
	}
	
	
	/**
	* Returns the second card of this hand
	*/
	public Card getCard2()
	{
		return card2;
	}
	
	
	/**
	* Returns an array of the two hand cards
	*/
	public Card[] getCards()
	{
		Card[] cards = {card1, card2};
		return cards;
	}
	
	
	/**
	 * Returns a string representation of the hand
	 */
	public String toString()
	{
		String str = "[";
		str += card1.toString();
		str += ",";
		str += card2.toString();
		str += "]";
		return str;
	}
}

