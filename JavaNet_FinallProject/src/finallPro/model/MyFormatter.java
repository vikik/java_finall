package finallPro.model;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {

	@Override
	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer(1000);
		
		buf.append(LocalDate.now().getDayOfMonth());
		buf.append("/");
		buf.append(LocalDate.now().getMonthValue());
		buf.append("/");
		buf.append(LocalDate.now().getYear());
		buf.append(" ");
		buf.append(LocalTime.now().getHour());
		buf.append(":");
		buf.append(LocalTime.now().getMinute());
		buf.append(":");
		buf.append(LocalTime.now().getSecond());
        buf.append(" ");
        buf.append(rec.getLevel());
        buf.append("\t-->\t");
        buf.append(formatMessage(rec));
        buf.append("\n");
        
        return buf.toString();
	}
}


