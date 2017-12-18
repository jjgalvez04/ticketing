package com.galvez.demos.ticketing;

import java.util.Date;

/**
 * An Event object represents an event with a name, a type and a date. 
 * 
 * @author jgalve
 *
 */
public interface Event extends TicketService {

	public String getEventName();

	public Date getEventDate();

	public int seatsAvailable();

	public EventType getEventType();

}
