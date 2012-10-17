package nl.starapple.poker;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class that reads and handles saved histories of played matches. This is the engine behind the visualization of poker
 * matches. It contains a MatchViewer object which is used for the visual representation.
 */
public class HistoryPlayer
{
	private String fileName;
	private FileReader fileReader;
	private ArrayList<String> currentHand;
	private String[] lineParts;
	
	private MatchViewer viewer;
	private long timeBetweenEvents;
	private String gameType;
	private String timeBank;
	private String timeTurn;
	private int numberOfBots;
	private ArrayList<String> botNames;
	
	
	/**
	 * Creates a HistoryPlayer that is able to read and handle saved match histories
	 */
	public HistoryPlayer(String file)
	{
		fileName = "";
		currentHand = new ArrayList<String>();
		lineParts = new String[3];
		viewer = new MatchViewer();
		timeBetweenEvents = 1000;
		botNames = new ArrayList<String>();
		
		openHistory(file);
		openHand(1);
		autoPlayHand();
	}
	
	
	/**
	 * Opens the history file with the given file name.
	 * @param file : the file name
	 */
	private void openHistory(String file)
	{
		fileName = file;
		try
		{
			fileReader = new FileReader(fileName);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		Scanner scanner = new Scanner(fileReader);
		String line;
		while(scanner.hasNextLine())
		{
			line = scanner.nextLine();
			if(line.startsWith("Settings"))
			{
				lineParts = line.split(" ");
				if(lineParts[1].equals("gameType"))
					gameType = lineParts[2];
				else if(lineParts[1].equals("timeBank"))
					timeBank = lineParts[2];
				else if(lineParts[1].equals("timeTurn"))
					timeTurn = lineParts[2];
				else if(lineParts[1].equals("players"))
					numberOfBots = Integer.valueOf(lineParts[2]);
				else if(lineParts[1].startsWith("seat"))
					botNames.add(lineParts[2]);
			}
			else
			{
				viewer.setupPlayers(botNames);
				break;
			}
		}
	}
	
	
	/**
	 * Stores the complete history of a single hand from the history file into an arraylist of strings.
	 * @param number : the hand number
	 */
	private boolean openHand(int number)
	{
		Scanner scanner = new Scanner(fileReader);
		String searchString = "Match hand " + number;
		while(scanner.hasNextLine())
			if(scanner.nextLine().endsWith(searchString))
				break;
		if(!scanner.hasNextLine())
		{
			System.err.println("The requested hand number is not in the file");
			return false;
		}
		
		String line = scanner.nextLine();
		while(scanner.hasNextLine() && !line.startsWith("Match hand"))
			currentHand.add(line);
		
		scanner.close();
		return true;
	}
	
	
	/**
	 * Sets the time between events for the autoplay modus
	 * @param time
	 */
	private void setTimeBetweenEvents(long time)
	{
		timeBetweenEvents = time;
	}
	
	
	/**
	 * Plays the current hand automatically with the given time between each event
	 */
	private void autoPlayHand()
	{
		int index = 0;
		while(playEvent(index++))
		{
			try{Thread.sleep(timeBetweenEvents);}
			catch (InterruptedException e){e.printStackTrace();}
		}
	}
	
	
	/**
	 * Reads the given event line of the current hand and gives the retrieved information to the MatchViewer. Sometimes
	 * multiple lines are read at once when they contain information that is to be send in one piece.
	 * @param lineIndex : the line number of the current hand
	 */
	private boolean playEvent(int lineIndex)
	{
		if(lineIndex >= currentHand.size())
			return false;
		
		String[] lineParts = new String[3];
		lineParts = currentHand.get(lineIndex).split(" ");	
		if(lineParts[0].equals("Match"))
		{
			if(lineParts[1].equals("hand"))
				viewer.updateRound(Integer.valueOf(lineParts[2]));
			else if(lineParts[1].equals("pot"))
			{
				ArrayList<Integer> allPots = new ArrayList<Integer>();
				allPots.add(Integer.valueOf(lineParts[2]));
				while(true)
				{
					lineParts = currentHand.get(++lineIndex).split(" ");
					if(lineParts[1].startsWith("sidepot"))
						allPots.add(Integer.valueOf(lineParts[2]));
					else
					{
						lineParts = currentHand.get(--lineIndex).split(" ");
						break;
					}
				}
				viewer.collectPots(allPots);
			}
			else if(lineParts[1].equals("table"))
				viewer.updateTable(lineParts[2]);
		}
		else
		{
			if(lineParts[1].equals("stack"))
			{
				ArrayList<String> botNames = new ArrayList<String>();
				ArrayList<Integer> botStacks = new ArrayList<Integer>();
				botNames.add(lineParts[0]);
				botStacks.add(Integer.valueOf(lineParts[2]));
				while(true)
				{
					lineParts = currentHand.get(++lineIndex).split(" ");
					if(lineParts[1].equals("stack"))
					{
						botNames.add(lineParts[0]);
						botStacks.add(Integer.valueOf(lineParts[2]));
					}
					else
					{
						lineParts = currentHand.get(--lineIndex).split(" ");
						break;
					}
				}
				viewer.setStacks(botNames, botStacks);
			}
			else if(lineParts[1].equals("sb"))
				viewer.playerBet(lineParts[0], Integer.valueOf(lineParts[2]));
			else if(lineParts[1].equals("bb"))
				viewer.playerBet(lineParts[0], Integer.valueOf(lineParts[2]));
			else if(lineParts[1].equals("hand"))
			{
				ArrayList<String> botNames = new ArrayList<String>();
				ArrayList<String> botHands = new ArrayList<String>();
				botNames.add(lineParts[0]);
				botHands.add(lineParts[2]);
				while(true)
				{
					lineParts = currentHand.get(++lineIndex).split(" ");
					if(!lineParts[0].equals("Match") && lineParts[1].equals("hand"))
					{
						botNames.add(lineParts[0]);
						botHands.add(lineParts[2]);
					}
					else
					{
						lineParts = currentHand.get(--lineIndex).split(" ");
						break;
					}
				}
				viewer.dealHands(botNames, botHands);
			}
			else if(lineParts[1].equals("wins"))
				viewer.returnPot(lineParts[0], Integer.valueOf(lineParts[2]));
			else if(lineParts[1].equals("fold"))
				viewer.playerFold(lineParts[0]);
			else if(lineParts[1].equals("check"))
				viewer.playerCheck(lineParts[0]);
			else if(lineParts[1].equals("call") || lineParts[1].equals("raise"))
				viewer.playerBet(lineParts[0], Integer.valueOf(lineParts[2]));				
		}
		return true;
	}
}
