package finallPro.model;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class CustomerFilter implements Filter {

	private String name;

	public CustomerFilter(String name) {
		this.name = name;
	}

	@Override
	public boolean isLoggable(LogRecord rec) {
		String[] msg = rec.getMessage().split(" ");
		for(int i = 0; i < msg.length; i++){
			if (msg[i].equals(name)){
				return true;
			}			
		}
		return false;
	}
}