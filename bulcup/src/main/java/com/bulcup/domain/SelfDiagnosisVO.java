package com.bulcup.domain;

import lombok.Data;

@Data
public class SelfDiagnosisVO {
	private Integer id;
	private String name;
	private Integer height;
	private Integer weight;
	private Integer age;
	private String self_diagnosis_data;
	private String self_diagnosis_answer;
}
