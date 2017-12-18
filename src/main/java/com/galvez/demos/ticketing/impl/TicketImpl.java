package com.galvez.demos.ticketing.impl;

import com.galvez.demos.ticketing.Ticket;
import com.galvez.demos.ticketing.TicketStatus;
import com.galvez.demos.ticketing.exceptions.TicketUnavailableException;

/**
 * Implementation of Ticket
 * 
 * @author jgalve
 *
 */
public class TicketImpl implements Ticket {

	private double ticketPrice;
	private int seatNumber;
	private String seatRow;
	private TicketStatus ticketStatus;

	public TicketImpl(String seatRow, int seatNumber, double price) {
		this.seatRow = seatRow;
		this.seatNumber = seatNumber;
		this.ticketPrice = price;
		ticketStatus = TicketStatus.AVAILABLE;
	}

	public double getTicketPrice() {
		return ticketPrice;
	}

	public int getSeatNumber() {
		return seatNumber;
	}

	public String getSeatRow() {
		return seatRow;
	}

	public TicketStatus getStatus() {
		return ticketStatus;
	}

	public void reserveTicket() throws TicketUnavailableException {
		if (ticketStatus != TicketStatus.AVAILABLE) {
			throw new TicketUnavailableException("Ticket is not available");
		}
		ticketStatus = TicketStatus.RESERVED;
	}

	public void purchaseTicket() throws TicketUnavailableException {
		if (ticketStatus != TicketStatus.RESERVED) {
			throw new TicketUnavailableException("Ticket is not reserved");
		}
		ticketStatus = TicketStatus.SOLD;
	}

	public void releaseTicket() throws TicketUnavailableException {
		if (ticketStatus != TicketStatus.RESERVED) {
			throw new TicketUnavailableException("Ticket is not reserved");
		}
		ticketStatus = TicketStatus.AVAILABLE;
	}

	@Override
	public String toString() {
		return String.format("Row %s, Seat %d", getSeatRow(), getSeatNumber());
	}

}
