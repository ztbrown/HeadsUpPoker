package nl.starapple.poker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a single deck of cards, which is shuffled in random order.
 * Cards can be drawn from the deck.
 */
public class Deck
{
	private List<Integer> cardOrder;
	
	
	/**
	 * Creates a new deck of 52 cards, represented by integers 0 to 51, which are
	 * then shuffled.
	 */
	public Deck()
	{
		cardOrder = new ArrayList<Integer>();
		for(int i = 0; i < 52; i++)
			cardOrder.add(i);
		
		Collections.shuffle(cardOrder);
	}
	
	
	/**
	 * Refreshes the deck such that it is a shuffled deck of 52 cards again
	 */
	public void resetDeck()
	{
		cardOrder = new ArrayList<Integer>();
		for(int i = 0; i < 52; i++)
			cardOrder.add(i);
		
		Collections.shuffle(cardOrder);
	}
	
	
	/**
	 * Pushes and returns the next card from the deck.
	 */
	public Card nextCard()
	{
		if(cardOrder.size() <= 0)
		{
			System.err.println("The deck is empty");
			return null;
		}
			
		int nextCardNumber = cardOrder.remove(cardOrder.size() - 1);
		Card card = new Card(nextCardNumber);
		return card;
	}
}
