package com.bulcup.domain;

import lombok.Data;

@Data
public class noticeVO {
	private int id;
	private String title;
	private String reg_date;
	private String writer;
	private String content;
}
