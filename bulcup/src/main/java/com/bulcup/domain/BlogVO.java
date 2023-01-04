package com.bulcup.domain;

import lombok.Data;

@Data
public class BlogVO {
	private String url;
	private String title;
	private String img;
	private String writer;
	private String time;
	private String content;
	private String classify;
}
