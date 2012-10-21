package nl.starapple.poker;
/**
 * Class that represents one Robot object and stores additional information such as the name that the bot receives and
 * which person is the author.
 */
public class PokerBot
{
	private Robot bot;
	private String name;
	private String author;
	
	public PokerBot(Robot bot, String name, String author)
	{
		this.bot = bot;
		this.name = name;
		this.author = author;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getAuthor()
	{
		return author;
	}
	
	public Robot getBot()
	{
		return bot;
	}
}
