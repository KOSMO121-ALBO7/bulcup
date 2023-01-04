package com.bulcup.domain;

import lombok.Data;

@Data
public class ContactVO {
	private int id;
	private String email;
	private String question;
	private String contact_date;
	private String answer;
	private String answer_date;
	private String manager_id;
}
