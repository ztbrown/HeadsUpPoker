package nl.starapple.io;

import java.io.IOException;

import nl.starapple.poker.HandInfo;
import nl.starapple.poker.HandResultInfo;
import nl.starapple.poker.MatchInfo;
import nl.starapple.poker.PokerMove;
import nl.starapple.poker.Robot;

public class IORobot implements Robot {

	IOHandler handler;

	public IORobot(String command) throws IOException {
		handler = new IOHandler(command);
	}

	@Override
	public void setup(long timeOut)
	{
		handler.readLine(timeOut);
	}
	
    @Override
	public void writeMove(PokerMove move) {
        handler.writeLine(move.toString());
    }
	
    @Override
	public PokerMove getMove(long timeOut) {
		handler.writeLine("go "+timeOut);
        String line = handler.readLine(timeOut);
        if( line == null ) { return null; }
        String[] parts = line.split("\\s");
        assert( parts.length == 2 ) : String.format("Bot input ``%s'' does not split into two parts", line);
        return new PokerMove(parts[0], Integer.valueOf(parts[1]));
    }
    
    @Override
	public void writeInfo(MatchInfo info) {
    	handler.writeLine(info.toString());
	}

    @Override
	public void writeInfo(HandInfo info) {
        handler.writeLine(info.toString());
    }
    
    @Override
	public void writeResult(HandResultInfo info) {
    	handler.writeLine(info.toString());
	}
	
	public void finish() {
		handler.stop();
	}

	public String getStdin() {
		return handler.getStdin();
	}

	public String getStdout() {
		return handler.getStdout();
	}

	public String getStderr() {
		return handler.getStderr();
	}
}
