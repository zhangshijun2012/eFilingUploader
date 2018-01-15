package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ImprotDatas {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/*School school = new School("分水", "万州");
		school.print();
		System.out.println("zhangjun");
		System.out.println("----------------------------------------");
		School school2 = school;
		school2.setName("三证");
		school2.print();*/
		File file = new File("D://zhang.txt");
		FileWriter writer = new FileWriter(file);
		writer.write("sssssssssssssss");
		writer.flush();
	}

}
class School {
	public String name;
	public String address;
	public School(String name, String address) {
		this.name = name;
		this.address = address;
	}
	public void print() {
		System.out.println("name = " + this.name + " address = " + this.address );
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
}
