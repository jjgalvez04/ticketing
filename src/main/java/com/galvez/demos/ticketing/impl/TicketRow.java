package com.galvez.demos.ticketing.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.galvez.demos.ticketing.Ticket;
import com.galvez.demos.ticketing.TicketStatus;
import com.galvez.demos.ticketing.exceptions.TicketException;
import com.galvez.demos.ticketing.exceptions.TicketUnavailableException;

/**
 * Representation of a TicketRow. This class is capable of finding the best
 * ticket(s) within the row, starting from the center and moving to the sides
 * 
 * @author jgalve
 *
 */
public class TicketRow {
	private TreeMap<Integer, Ticket> tickets;
	private int maxContiguousTickets;
	private TreeMap<Integer, Ticket> maxContiguousTicketsMap;
	private String rowId;
	private boolean maxContiguousTicketsFresh;

	/**
	 * Creates a new TicketRow
	 * 
	 * @param rowId
	 *            Row id, it can be A, B, C, D...
	 */
	public TicketRow(String rowId) {
		this.rowId = rowId;
		tickets = new TreeMap<Integer, Ticket>();
	}

	/**
	 * Returns a List with number of tickets requested seating together
	 * 
	 * @param requestedNumber
	 *            number of tickets requested
	 * @return List<Ticket> with the tickets requested
	 * @throws TicketUnavailableException
	 *             if there are not enough tickets together for this request. To
	 *             avoid it request {@link #getMaxContiguousTickets()} first
	 */
	public List<Ticket> getTickets(int requestedNumber) throws TicketUnavailableException {
		if (requestedNumber > getMaxContiguousTickets()) {
			throw new TicketUnavailableException("There are not enough tickets available in this row");
		}

		return findBestTickets(requestedNumber);
	}

	/**
	 * Finds the best tickets in the row. If the row is empty it will start with the
	 * ticket in the middle and get one ticket at a time right and left until it has
	 * all the tickets needed. If there are already tickets unavailable, it will
	 * find the biggest place where there are enough tickets and find the most
	 * centered ones and return them
	 * 
	 * @param requestedNumber
	 *            total number of tickets requested
	 * @return a list with the tickets to reserve
	 */
	private synchronized List<Ticket> findBestTickets(int requestedNumber) {
		maxContiguousTicketsFresh = false;
		if (maxContiguousTickets == requestedNumber) {
			return new ArrayList<Ticket>(maxContiguousTicketsMap.values());
		}

		List<Ticket> bestTickets = new ArrayList<Ticket>();
		// Get the ticket in the middle.
		Ticket midContiguousTicket = getMiddleTicket(maxContiguousTicketsMap);
		// Let's see where we are in the row.
		Ticket midRow = getMiddleTicket(tickets);
		if (midContiguousTicket.getSeatNumber() == midRow.getSeatNumber()) {
			// The row is empty, get the tickets one at a time from left and right
			bestTickets.add(midContiguousTicket);
			addSeatsBothSides(requestedNumber - 1, bestTickets, midContiguousTicket.getSeatNumber());
			return bestTickets;
		}

		// We define whether we are to the left or right to get the tickets centered
		Iterator<Integer> iterator = null;
		if (midContiguousTicket.getSeatNumber() < midRow.getSeatNumber()) {
			TreeSet<Integer> sortedKeys = new TreeSet<Integer>(maxContiguousTicketsMap.keySet());
			iterator = sortedKeys.descendingIterator();
		} else if (midContiguousTicket.getSeatNumber() > midRow.getSeatNumber()) {
			iterator = maxContiguousTicketsMap.keySet().iterator();
		}

		int remainingTickets = requestedNumber;
		while (iterator.hasNext() && remainingTickets > 0) {
			bestTickets.add(maxContiguousTicketsMap.get(iterator.next()));
			remainingTickets--;
		}

		return bestTickets;
	}

	/**
	 * Adds tickets to bestTickets one at a time starting with the right of the
	 * midPoint
	 * 
	 * @param neededTickets
	 *            total number of tickets needed
	 * @param bestTickets
	 *            list to add the tickets
	 * @param midPoint
	 *            ticket number of the ticket in the middle
	 */
	private void addSeatsBothSides(int neededTickets, List<Ticket> bestTickets, int midPoint) {
		int neededLeft = neededTickets / 2;
		int neededRight = neededTickets - neededLeft;
		for (int i = 1; i <= neededRight; i++) {
			bestTickets.add(maxContiguousTicketsMap.get(midPoint + i));
		}
		for (int i = 1; i <= neededLeft; i++) {
			bestTickets.add(maxContiguousTicketsMap.get(midPoint - i));
		}
	}

	/**
	 * Finds the ticket in the middle of any given list of tickets
	 * 
	 * @param map
	 *            Map with all the tickets to find the one in the middle
	 * @return
	 */
	private Ticket getMiddleTicket(TreeMap<Integer, Ticket> map) {
		int firstSeat = 0;
		Iterator<Integer> iterator = map.keySet().iterator();
		if (iterator.hasNext()) {
			firstSeat = map.get(iterator.next()).getSeatNumber();
		}
		return map.get(firstSeat + (map.size() / 2));
	}

	/**
	 * Returns the maximum number of tickets seating together in this row
	 * 
	 * @return the maximum number of contiguous tickets in the row, zero if there
	 *         are none.
	 */
	public int getMaxContiguousTickets() {
		if (maxContiguousTicketsFresh) {
			return maxContiguousTickets;
		} else {
			maxContiguousTickets = 0;
			maxContiguousTicketsMap = new TreeMap<Integer, Ticket>();
		}

		TreeMap<Integer, Ticket> maxTicketsMap = new TreeMap<Integer, Ticket>();
		int maxTickets = 0;
		Iterator<Integer> iterator = tickets.keySet().iterator();
		while (iterator.hasNext()) {
			Ticket ticket = tickets.get(iterator.next());
			if (ticket.getStatus() == TicketStatus.AVAILABLE) {
				maxTickets++;
				maxTicketsMap.put(ticket.getSeatNumber(), ticket);
			} else {
				if (maxTickets > maxContiguousTickets) {
					maxContiguousTicketsMap = maxTicketsMap;
					maxContiguousTickets = maxTickets;
				} else {
					maxTickets = 0;
					maxTicketsMap.clear();
				}
			}
		}
		if (maxTickets > maxContiguousTickets) {
			maxContiguousTicketsMap = maxTicketsMap;
			maxContiguousTickets = maxTickets;
		}
		maxContiguousTicketsFresh = true;
		return maxContiguousTickets;
	}

	/**
	 * Adds a seat to this row
	 * 
	 * @param ticket
	 *            Ticket to be added
	 * @throws TicketException
	 *             if the ticket row does not match this row id
	 */
	public void addSeat(Ticket ticket) throws TicketException {
		if (ticket.getSeatRow() != getRowId()) {
			throw new TicketException("Ticket does not belong to this row");
		}
		tickets.put(ticket.getSeatNumber(), ticket);
	}

	/**
	 * Returns the identifier of this row.
	 * 
	 * @return String representing the row Id: A, B, C, D...
	 */
	public String getRowId() {
		return rowId;
	}

}
