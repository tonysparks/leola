/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoNamespace;

/**
 * Date functions
 *
 * @author Tony
 *
 */
public class DateLeolaLibrary implements LeolaLibrary {

	/**
	 * The runtime
	 */
	private Leola runtime;
		
	/* (non-Javadoc)
	 * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
	 */
	@LeolaIgnore
	public void init(Leola runtime, LeoNamespace namespace) throws Exception {		
		this.runtime = runtime;	
		this.runtime.putIntoNamespace(this, namespace);		
	}
			
	/**
	 * Formats the supplied {@link Date}
	 * @param date
	 * @param format
	 * @return the formated string
	 */
	public String format(Date date, String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);		
		return dateFormat.format(date);
	}
	
	/**
	 * Sets the default time zone
	 * 
	 * @param timeZone
	 */
	public void setTimeZone(String timeZone) {
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
	}
	
	/**
	 * @return retrieves the default time zone
	 */
	public String getTimeZone() {
		return TimeZone.getDefault().getID();
	}
	
	/**
	 * @param date
	 * @return the msec
	 */
	public int getMSec(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);		
		return c.get(Calendar.MILLISECOND);
	}
	
	/**
	 * @param date
	 * @return the second
	 */
	public int getSecond(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);		
		return c.get(Calendar.SECOND);
	}
	
	/**
	 * @param date
	 * @return the minute
	 */
	public int getMinute(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);		
		return c.get(Calendar.MINUTE);
	}
	
	/**
	 * @param date
	 * @return the hour
	 */
	public int getHour(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);		
		return c.get(Calendar.HOUR);
	}
	
	/**
	 * @param date
	 * @return the day
	 */
	public int getDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);		
		return c.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * @param date
	 * @return the month
	 */
	public int getMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);		
		return c.get(Calendar.MONTH);
	}
	
	/**
	 * @param date
	 * @return the Year
	 */
	public int getYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);		
		return c.get(Calendar.YEAR);
	}
	
	/**
	 * @param date
	 * @return the era
	 */
	public int getEra(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);		
		return c.get(Calendar.ERA);
	}
	/**
	 * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object.
	 * @param date
	 * @return
	 */
	public long toMSec(Date date) {
		return date.getTime();
	}
	
	/**
	 * Converts the {@link Date} to a {@link Timestamp}
	 * @param date
	 * @return
	 */
	public Timestamp toTimestamp(Date date) {
		return new Timestamp(date.getTime());
	}

	/**
	 * @param time
	 * @return the supplied time to a {@link Date} object
	 */
	public Date toDate(long time) {
		return new Date(time);
	}
	
	/**
	 * @return the current time in milliseconds
	 */
	public long time() {
		return System.currentTimeMillis();
	}
	
	/**
	 * @return the current time in nanoseconds
	 */
	public long timens() {
		return System.nanoTime();
	}
	
	/**
	 * @return the current {@link Date}
	 */
	public Date now() {
		return new Date();
	}
	
	private Date adjustDate(int type, Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date); 
		c.add(type, amount);
		return c.getTime();
	}
	
	/**
	 * Add/subtract the number of milli-seconds to the supplied date
	 * 
	 * @param date
	 * @param msec
	 * @return the adjusted {@link Date}
	 */
	public Date addMSecs(Date date, int msec) {
		return adjustDate(Calendar.MILLISECOND, date, msec);
	}
	
	/**
	 * Add/subtract the number of seconds to the supplied date
	 * 
	 * @param date
	 * @param seconds
	 * @return the adjusted {@link Date}
	 */
	public Date addSeconds(Date date, int seconds) {
		return adjustDate(Calendar.SECOND, date, seconds);
	}
	
	/**
	 * Add/subtract the number of minutes to the supplied date
	 * 
	 * @param date
	 * @param minutes
	 * @return the adjusted {@link Date}
	 */
	public Date addMinutes(Date date, int minutes) {
		return adjustDate(Calendar.MINUTE, date, minutes);
	}
	
	/**
	 * Add/subtract the number of hours to the supplied date
	 * 
	 * @param date
	 * @param hours
	 * @return the adjusted {@link Date}
	 */
	public Date addHours(Date date, int hours) {
		return adjustDate(Calendar.HOUR, date, hours);
	}
	
	/**
	 * Add/subtract the number of days to the supplied date
	 * 
	 * @param date
	 * @param days
	 * @return the adjusted {@link Date}
	 */
	public Date addDays(Date date, int days) {
		return adjustDate(Calendar.DATE, date, days);
	}
	
	/**
	 * Add/subtract the number of months to the supplied date
	 * 
	 * @param date
	 * @param months
	 * @return the adjusted {@link Date}
	 */
	public Date addMonths(Date date, int months) {
		return adjustDate(Calendar.MONTH, date, months);
	}
	
	/**
	 * Add/subtract the number of Years to the supplied date
	 * 
	 * @param date
	 * @param years
	 * @return the adjusted {@link Date}
	 */
	public Date addYears(Date date, int years) {
		return adjustDate(Calendar.YEAR, date, years);
	}
	
	/**
	 * Add/subtract the era to the supplied date
	 * 
	 * @param date
	 * @param era
	 * @return the adjusted {@link Date}
	 */
	public Date addEra(Date date, int era) {
		return adjustDate(Calendar.ERA, date, era);
	}

}

