package com.bulcup.domain;

import lombok.Data;

@Data
public class RawMaterialVO {
	Integer	id;
	String	raw_material_name;
	String	functionality;
	String	caution;
	double	daily_intake_min;
	double daily_intake_max;
	String	unit;
}
