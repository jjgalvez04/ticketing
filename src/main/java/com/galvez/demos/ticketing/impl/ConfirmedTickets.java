package com.galvez.demos.ticketing.impl;

import java.util.List;

import com.galvez.demos.ticketing.Ticket;

public class ConfirmedTickets {

	private String confirmationCode;

	private String confirmationEmail;

	private List<Ticket> tickets;

	/**
	 * Confirmation for the tickets purchased.
	 * 
	 * @param confirmationCode
	 *            Unique identifier for this confirmation
	 * @param confirmationEmail
	 *            Email address owning this confirmation
	 * @param tickets
	 *            List of tickets in this confirmation
	 */
	public ConfirmedTickets(String confirmationCode, String confirmationEmail, List<Ticket> tickets) {
		this.confirmationCode = confirmationCode;
		this.confirmationEmail = confirmationEmail;
		this.tickets = tickets;
	}

	public String getConfirmationCode() {
		return confirmationCode;
	}

	public String getConfirmationEmail() {
		return confirmationEmail;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

}
