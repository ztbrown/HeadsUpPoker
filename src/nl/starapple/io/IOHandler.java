package nl.starapple.io;

import java.io.IOException;

public class IOHandler {

	Process child;
	InStream out, err;
	OutStream in;
	
	public IOHandler(String command) throws IOException {
		child = Runtime.getRuntime().exec(command);
		in = new OutStream(child.getOutputStream());
		out = new InStream(child.getInputStream());
		err = new InStream(child.getErrorStream());
		out.start(); err.start();
	}
	
	public void stop() {
		try {
			in.close();
			Thread.sleep(100);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		out.finish();
		err.finish();
		child.destroy();
		try {
			child.waitFor();
			out.join();
			err.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String readLine(long timeout) {
		//System.err.printf("readLine(%d)\n", timeout);
		try {
			in.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return out.readLine(timeout);
	}
	
	public boolean writeLine(String line) {
		//System.err.printf("writeLine(\"%s\")\n", line);
		try {
			in.writeLine(line.trim());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String getStdin() {
		return in.getData();
	}
	
	public String getStdout() {
		return out.getData();
	}
	
	public String getStderr() {
		return err.getData();
	}
}
