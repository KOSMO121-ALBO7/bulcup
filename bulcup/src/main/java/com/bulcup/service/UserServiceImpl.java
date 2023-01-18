package com.bulcup.service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bulcup.dao.UserDao;
import com.bulcup.domain.BlogVO;
import com.bulcup.domain.CategoryVO;
import com.bulcup.domain.ContactVO;
import com.bulcup.domain.FunctionalFoodVO;
import com.bulcup.domain.SubscribeVO;
import com.bulcup.domain.SympthomQuestionVO;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userdao;

	public int insertContact(ContactVO vo) {
		return userdao.insertContact(vo);
	}

	public List<BlogVO> getBlogList(int pageNo, int pageSize) {
		List<BlogVO> list = new ArrayList<BlogVO>();
		Scanner sc = null;
		try {
			sc = new Scanner(new FileInputStream("src/main/java/com/bulcup/service/crawling/text.txt"), "UTF-8");
			String line = sc.nextLine();// header 읽기

			int lineEnd = pageNo * pageSize;
			int linePrint = 1;
			int lineStart = (pageNo - 1) * pageSize;
			System.out.println("STARTN END " + lineStart + " " + lineEnd);
			while (sc.hasNext() && linePrint <= lineEnd) {
				line = sc.nextLine();
				if (linePrint > lineStart) {
					System.out.println("lineprint : " + linePrint);
					// System.out.println(line);
					String[] arr = line.split("::");
					BlogVO vo = new BlogVO();
					vo.setUrl(arr[0]);
					vo.setTitle(arr[1]);
					vo.setImg(arr[2]);
					vo.setWriter(arr[3]);
					vo.setTime(arr[4]);
					vo.setContent(arr[5]);
					vo.setClassify(arr[6]);
					list.add(vo);
				}
				linePrint++;
			} // while

		} catch (Exception e) {
			System.out.println("Exception GETBLOGLIST: " + e);
		} finally {
			sc.close();
		}
		return list;
	}// getbloglist

	public List<FunctionalFoodVO> getFunctionalFoodListPg(HashMap map) {
		return userdao.getFunctionalFoodListPg(map);
	}

	public Integer foodCount(Integer category_id) {
		return userdao.foodCount(category_id);
	}

	public Integer foodCount(HashMap searchMap) {
		return userdao.foodCountBySearch(searchMap);
	}

	public List<CategoryVO> category() {
		return userdao.category();
	}

	public int insertSubscriber(SubscribeVO vo) {
		return userdao.insertSubscriber(vo);
	}

	public List<SympthomQuestionVO> getSympthomQuestion(String category_id) {
		return userdao.getSympthomQuestion(category_id);
	}


}
