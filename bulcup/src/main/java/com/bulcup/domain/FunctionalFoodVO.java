package com.bulcup.domain;

import lombok.Data;

@Data
public class FunctionalFoodVO {
	private int id;
	private int category_id;
	private String category;
	private String manufacturer;
	private String functional_food_name;
	private String functionalities;
	private String raw_materials;
	private String caution;
	private String formulation;
	private String intake_method;
	private String image_path;
}
