package com.bulcup.domain;

import lombok.Data;

@Data
public class LifestyleQuestionVO {
	private int 	id;
	private int		lifestyle_group_id;
	private String	lifestyle_group;
	private String	lifestyle_question;
}
