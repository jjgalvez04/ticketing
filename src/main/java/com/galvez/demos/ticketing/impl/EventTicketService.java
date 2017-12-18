package com.galvez.demos.ticketing.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.galvez.demos.ticketing.Event;
import com.galvez.demos.ticketing.EventType;
import com.galvez.demos.ticketing.SeatHold;
import com.galvez.demos.ticketing.Ticket;
import com.galvez.demos.ticketing.TicketService;
import com.galvez.demos.ticketing.TicketStatusListener;
import com.galvez.demos.ticketing.exceptions.TicketException;
import com.galvez.demos.ticketing.exceptions.TicketUnavailableException;

/**
 * EventTicketService represents an event with the ability to reserve and sell
 * tickets for the event
 * 
 * @author jgalve
 *
 */
public class EventTicketService implements TicketService, Event, TicketStatusListener {

	private Date eventDate;

	private String eventName;

	private EventType eventType;

	private Map<String, TicketRow> availableTickets;

	private Map<Integer, SeatHold> reservedTickets;

	private Map<String, ConfirmedTickets> purchasedTickets;

	private int totalTicketsAvailable;

	/**
	 * Creates a new Event
	 * 
	 * @param eventName
	 *            Name for the event
	 * @param eventDate
	 *            Date when the event will happen
	 * @param eventType
	 *            Type of the event, either Screen or Stage based
	 */
	private EventTicketService(String eventName, Date eventDate, EventType eventType) {
		this.eventDate = eventDate;
		this.eventName = eventName;
		this.eventType = eventType;
		availableTickets = new HashMap<String, TicketRow>();
		reservedTickets = new HashMap<Integer, SeatHold>();
		purchasedTickets = new HashMap<String, ConfirmedTickets>();
	}

	/**
	 * Creates a new Event with the specified tickets
	 * 
	 * @param eventName
	 *            Name for the event
	 * @param eventDate
	 *            Date when the event will happen
	 * @param eventType
	 *            Type of the event, either Screen or Stage based
	 * @param tickets
	 *            seats that will be available for this event
	 */
	public EventTicketService(String eventName, Date eventDate, EventType eventType, List<Ticket> tickets) {
		this(eventName, eventDate, eventType);
		Map<String, TicketRow> availableTickets = new HashMap<String, TicketRow>();

		for (Ticket ticket : tickets) {
			addTicket(ticket);
		}

		this.availableTickets = availableTickets;
	}

	/**
	 * Creates a new Event with the specified tickets
	 * 
	 * @param eventName
	 *            Name for the event
	 * @param eventDate
	 *            Date when the event will happen
	 * @param eventType
	 *            Type of the event, either Screen or Stage based
	 * @param availableTickets
	 *            seats that will be available for this event
	 */
	public EventTicketService(String eventName, Date eventDate, EventType eventType,
			Map<String, TicketRow> availableTickets) {
		this(eventName, eventDate, eventType);
		this.availableTickets = availableTickets;
		totalTicketsAvailable = availableTickets.size();
	}

	/**
	 * Creates a new event with the specified parameters to build the ticket map
	 * 
	 * @param eventName
	 *            Name for the event
	 * @param eventDate
	 *            Date when the event will happen
	 * @param eventType
	 *            Type of the event, either Screen or Stage based
	 * @param rows
	 *            String array with the row Ids: A, B, C, D...
	 * @param seatsPerRow
	 *            Number of seats that will be available per row
	 * @param price
	 *            Default price for the tickets
	 */
	public EventTicketService(String eventName, Date eventDate, EventType eventType, String[] rows, int seatsPerRow,
			double price) {
		this(eventName, eventDate, eventType);
		for (int i = 0; i < rows.length; i++) {
			String rowId = rows[i];
			for (int j = 1; j <= seatsPerRow; j++) {
				Ticket ticket = new TicketImpl(rowId, j, price);
				addTicket(ticket);
			}
		}
	}

	private void addTicket(Ticket ticket) {
		TicketRow row = availableTickets.get(ticket.getSeatRow());
		if (row == null) {
			row = new TicketRow(ticket.getSeatRow());
			availableTickets.put(row.getRowId(), row);
		}
		try {
			row.addSeat(ticket);
			totalTicketsAvailable++;
		} catch (TicketException e) {
			// This shouldn't happen given that we are checking for this
			throw new RuntimeException(e);
		}
	}

	public int numSeatsAvailable() {
		return totalTicketsAvailable;
	}

	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) throws TicketUnavailableException {
		// First make sure we have enough tickets available
		if (numSeats > numSeatsAvailable()) {
			throw new TicketUnavailableException("There are not enough tickets available");
		}

		/*
		 * When the event is a screen based (Movie) then the worst seats are in the
		 * front while in a stage based the best seats are in the front
		 */
		TreeSet<String> sortedKeys = new TreeSet<String>(availableTickets.keySet());
		Iterator<String> iterator;
		if (getEventType() == EventType.SCREEN) {
			iterator = sortedKeys.descendingIterator();
		} else {
			iterator = sortedKeys.iterator();
		}

		// We'll now iterate through the rows finding the best seats in the best row
		List<Ticket> tickets = null;
		while (iterator.hasNext()) {
			String section = iterator.next();
			TicketRow ticketRow = availableTickets.get(section);
			if (ticketRow.getMaxContiguousTickets() >= numSeats) {
				try {
					tickets = ticketRow.getTickets(numSeats);
					break;
				} catch (TicketUnavailableException ex) {
					/*
					 * We shouldn't get here, but if we are running with multiple threads we could.
					 * In any case we can just move on to the next row
					 * 
					 */
				}
			}
		}

		if (tickets == null) {
			// We didn't find tickets in any row
			throw new TicketUnavailableException("There are not enough tickets available");
		}

		totalTicketsAvailable -= tickets.size();
		SeatHold hold = new SeatHoldImpl(tickets, customerEmail);
		hold.notifyTicketAvailableAgain(this);
		reservedTickets.put(hold.getSeatHoldId(), hold);
		return hold;
	}

	public String reserveSeats(int seatHoldId, String customerEmail) throws TicketException {
		SeatHold heldTickets = reservedTickets.get(seatHoldId);
		String confirmationCode = heldTickets.confirmSeats(customerEmail);

		if (confirmationCode == null) {
			throw new TicketException("The confirmation code and email do not match");
		}

		ConfirmedTickets tickets = new ConfirmedTickets(confirmationCode, customerEmail, heldTickets.getTickets());

		purchasedTickets.put(confirmationCode, tickets);
		return confirmationCode;
	}

	public String getEventName() {
		return eventName;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public int seatsAvailable() {
		return numSeatsAvailable();
	}

	public EventType getEventType() {
		return eventType;
	}

	public void notifyStatusChange(Ticket ticket) {
		switch (ticket.getStatus()) {
		case AVAILABLE:
			totalTicketsAvailable++;
			break;
		case RESERVED:
			totalTicketsAvailable--;
			break;
		default:
			break;
		}
	}

}
