package com.bulcup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class BulcupLogger {

	private static BulcupLogger instance;
	private File myObj;
	private boolean append = false;
	private FileWriter myWriter;
	private Scanner myReader;

	// name: getLogger
	// param: String=filename
	// returns: BulcupLogger
	// function: 새로운 파일(filename)을 만들며 로거 객체를 생성한다.
	public static BulcupLogger getLogger(String filename) {
		instance = new BulcupLogger(filename);
		return instance;
	}

	// name: getLogger
	// param: String=filename, int= 0
	// returns: BulcupLogger
	// function: 새로운 파일(filename)을 만들며 로거 객체를 생성한다.
	public static BulcupLogger getLogger(String filename, int zero) {
		instance = new BulcupLogger(filename, zero);
		return instance;
	}

	// name: BulcupLogger
	//function: filename 의 파일을 여는 BulcupLogger 객체이다
	private BulcupLogger(String filename) {
		myObj = new File(filename);
		try {
			if (myObj.createNewFile())
				;
			else
				append = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// name: BulcupLogger
	//function: filename 의 파일을 열어 0을 쓰는 BulcupLogger 객체이다
	private BulcupLogger(String filename, int zero) {
		myObj = new File(filename);
		System.out.println(myObj + " :myFILE");
		try {
			if (myObj.createNewFile()) {
				myWriter = new FileWriter(myObj); // createfile == true
				myWriter.write(String.valueOf(zero));
				System.out.println("WRITTEN " + String.valueOf(zero));// 이 줄 지워도 됨
				myWriter.close();
			} else
				append = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// name: log
	// param: String=msg
	// function: BulcupLogger(filename)에 msg를 쓴다.
	public void log(String msg) {
		try {
			myWriter = new FileWriter(myObj, append);
			myWriter.write(msg);
			System.out.println("writing msg : " + msg);
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// name: logIncrement
	// param: String=add
	// function: BulcupLogger(filename,0)에 add(문자열로 받아진 숫자)를 더한다.
	public void logIncrement(String add) {
		try {
			myReader = new Scanner(myObj);
			long numberInFile = myReader.nextLong(); // 원래 있던 숫자를 가져온다
			myReader.close();
			// temp 파일이 존재하는 이유: .swp 파일이라고 생각하면 편하다. 
			// 컴퓨터에서 정보를 옮길때 a = b, c = a, b = c 로 옮기는 것과 같은 것임
			File tempFile = new File("tempfile.log");
			if (!tempFile.createNewFile())
				tempFile.delete();
			// 새로 파일을 만들어 더해진 값을 저장한다
			Writer output = new FileWriter(tempFile);
			long numberToFile = numberInFile + Long.valueOf(add);
			output.write(String.valueOf(numberToFile));
			output.close();
			System.out.println("number : " + numberToFile + " " + numberInFile); // 이 줄 지워도 됨
			myObj.delete(); //원래 있던 파일을 지우고
			tempFile.renameTo(myObj); // 임시 파일을 원래 이름으로 저장한다
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// logIncrement

	// name: logIncrementTime
	// param: Long=addTime
	// function: BulcupLogger(filename,0)에 addTime(문자열로 받아진 숫자)를 더한다.
	//			이때 시간은 분단위로 계산되어 UserController 에서 넘어왔다.
	public void logIncrementTime(Long addTime) {
		System.out.println("-------------------------");// 이 줄 지워도 됨
		System.out.println("Logging time");// 이 줄 지워도 됨
		System.out.println("LOGGING TIME TO : " + myObj);// 이 줄 지워도 됨
		try {
			// 원래 파일에 저장 되어있던 시간에다가 addTime을 더해준다
			// 더해주기위해 tempFile을 하나 작성한다.
			File tempFile = new File("tempfile.log");
			if (!tempFile.createNewFile())
				tempFile.delete();
			myReader = new Scanner(myObj);
			long time = 0;
			if (myReader.hasNextLine()) {
				time = Long.valueOf(myReader.nextLine());
			}// 원래 써져있던 시간 값을 읽는다.
			myReader.close();
			
			System.out.println("TIME : " + time);
			System.out.println("addtime is : " + addTime);

			myWriter = new FileWriter(tempFile);
			// 원래 시간에 더해진 시간(addtime)을 더해준다.
			myWriter.write(String.valueOf(time + addTime));
			myWriter.close();
			myObj.delete();
			tempFile.renameTo(myObj);
			// temp 파일을 진짜 파일로 만들다.
			System.out.println("writing time : " + String.valueOf(time + addTime));// 이 줄 지워도 됨

			System.out.println("-------------------------");// 이 줄 지워도 됨
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// logtime

}