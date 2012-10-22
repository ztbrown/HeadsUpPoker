package nl.starapple.starterbot;

import nl.starapple.poker.PokerMove;

public class LegacyStarter implements Bot {

	LegacyRobot robot;
	
	public LegacyStarter(LegacyRobot robot) {
		this.robot = robot;
	}
	
	@Override
	public PokerMove getMove(PokerState state, Long timeOut) {
		String json = toJson(state);
		String actionStr = robot.makeBet(json);
		return toMove(actionStr);
	}

	private String toJson(PokerState state) {
		// TODO Auto-generated method stub
		return null;
	}

	private PokerMove toMove(String actionStr) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LegacyRobot myBot = null;
		// TODO: myBot = new MyRobot();
		Parser parser = new Parser(new LegacyStarter(myBot));
		parser.run();
	}

}
