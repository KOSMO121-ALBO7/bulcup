package com.bulcup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.Data;

@Data
public class paging {

	public static int pageSize = 21;//레코드갯수
	public static int getPageSize = countLines();
	public static int totalPageNo = getPageSize / pageSize;
	public static int pageNo = 1;

	public static int startRow;

	public static int countLines() {
		Path path = Paths.get("src/main/java/com/bulcup/service/crawling/text.txt");
		long lines = 0;
		try {
			lines = Files.lines(path).count();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return (int) lines;
	}
}