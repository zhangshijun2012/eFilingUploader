package test;

import java.io.File;
import java.io.FilenameFilter;

import javax.servlet.http.HttpServletResponse;

import com.sinosoft.filenet.FileEntry;
import com.sinosoft.filenet.FileIndex;
import com.sinosoft.filenet.FileNetConnection;
import com.sinosoft.util.FileHelper;
import com.sinosoft.util.StringHelper;

public class Test {

	public static void print(File file) {
		if (file.isFile()) {
			if (FileHelper.getFileSuffix(file).equalsIgnoreCase(".java")) {
				String txt = FileHelper.getText(file, "GBK");
				FileHelper.print(file, txt, false, "UTF-8");
			}
		} else {
			File[] files = file.listFiles();
			for (File f : files) {
				print(f);
			}
		}
	}

	public static void print() {
		File dir = new File("D:\\work\\workspace\\libaoSelenium\\src\\java\\org");
		print(dir);
	}

	public static void printHBMCfg() {
		File file = new File("D:\\work\\workspace\\eFiling\\src\\com\\sinosoft\\efiling\\hibernate\\entity");
		String[] files = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".hbm.xml");
			}
		});
		String line = "<mapping resource=\"com/sinosoft/efiling/hibernate/entity/{hbm}\" />";
		StringBuffer text = new StringBuffer();
		for (String fileName : files) {
			// fileName = fileName.substring(0, fileName.length() - 5);
			text.append(line.replace("{hbm}", fileName));
			text.append("\n");
		}
		System.out.println(text);
	}

	public static void printDaoCfg() {
		File file = new File("D:\\work\\workspace\\eFiling\\src\\com\\sinosoft\\efiling\\hibernate\\dao");
		String[] files = file.list();
		String line = "<bean id=\"{id}\" class=\"com.sinosoft.efiling.hibernate.dao.{dao}\" parent=\"entityDao\"></bean>";
		StringBuffer text = new StringBuffer();
		for (String fileName : files) {
			fileName = fileName.substring(0, fileName.length() - 5);
			text.append(line.replace("{dao}", fileName).replace("{id}",
					fileName.substring(0, 1).toLowerCase() + fileName.substring(1)));
			text.append("\n");
		}
		System.out.println(text);
	}

	public static void printServiceCfg() {
		File file = new File("D:\\work\\workspace\\eFiling\\src\\com\\sinosoft\\efiling\\service");
		String[] files = file.list();
		String line = "<bean id=\"{id}\" class=\"com.sinosoft.efiling.service.{service}\" parent=\"serviceSupport\">\n"
				+ "\t<property name=\"dao\"><ref local=\"{dao}\" /></property>\n" + "</bean>";
		StringBuffer text = new StringBuffer();
		for (String fileName : files) {
			fileName = fileName.substring(0, fileName.length() - 5);
			text.append(line
					.replace("{dao}",
							fileName.substring(0, 1).toLowerCase() + fileName.replace("Service", "Dao").substring(1))
					.replace("{service}", fileName)
					.replace("{id}", fileName.substring(0, 1).toLowerCase() + fileName.substring(1)));
			text.append("\n");
		}
		System.out.println(text);
	}

	public static void printActionCfg() {
		File file = new File("D:\\work\\workspace\\eFiling\\src\\com\\sinosoft\\efiling\\struts2\\action");
		String[] files = file.list();
		String line = "<bean id=\"{id}\" class=\"com.sinosoft.efiling.struts2.action.{action}\" parent=\"actionSupport\">\n"
				+ "\t<property name=\"service\"><ref local=\"{service}\" /></property>\n" + "</bean>";
		StringBuffer text = new StringBuffer();
		for (String fileName : files) {
			fileName = fileName.substring(0, fileName.length() - 5);
			text.append(line
					.replace("{service}",
							fileName.substring(0, 1).toLowerCase() + fileName.replace("Action", "Service").substring(1))
					.replace("{action}", fileName)
					.replace("{id}", fileName.substring(0, 1).toLowerCase() + fileName.substring(1)));
			text.append("\n");
		}
		System.out.println(text);
	}

	public HttpServletResponse getText(HttpServletResponse response) {
		HttpServletResponse res = response;

		return null;
	}

	public static void main(String[] args) {
		try {
			throw new ArrayIndexOutOfBoundsException(3);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
//		FileNetConnection con = new FileNetConnection();
//		FileEntry entry = new FileEntry();
//		FileIndex fileIndex = new FileIndex();
//		File file = new File("d:\\x.png");
//		fileIndex.setFileId(StringHelper.uuid());
//		fileIndex.setFileContentType(FileHelper.getContentType(file.getName()));
//		fileIndex.setFileName(file.getName());
//		fileIndex.setFileTitle(file.getName());
//		entry.setFileIndex(new FileIndex());
//		entry.setFiles(new File[] { file });
//		entry.setFilesContentType(new String[] { fileIndex.getFileContentType() });
//		entry.setFilesFileName(new String[] { fileIndex.getFileName() });
//		con.save(entry);
//		con.close();

		// print();
		// String documentNo = " X1Y2Z3:/'";
		// System.out.println(documentNo.replaceAll("\\D", ""));

		// Company c = new Company();
		// for (Method m : TreeEntitySupport.class.getMethods()) {
		// System.out.println(m.getName());
		// }
		// // printHBMCfg();
		// // printDaoCfg();
		// // printServiceCfg();
		// // FileBoxVersionAction a = new FileBoxVersionAction();
		//
		// // JSONObject o = new JSONObject(new Object[] { new Date() });
		// // System.out.println(o);
		// // System.out.println(Integer.toHexString(233));
		// System.out.print(Integer.toHexString(230));
		// System.out.print(Integer.toHexString(168));
		// System.out.println(Integer.toHexString(196));

		// String[] ids = { "B10001", "B10002", "B10003", "B10004", "B10005", "B10006", "B10007", "B10008", "B10009",
		// "B10010", "B20001", "B20002", "B20004", "B20005", "B20006", "B30004", "B30005", "B30006", "B30007",
		// "B30008", "B30009", "B30010", "B30011", "B30012", "B30013", "B30014", "B30015", "B30016", "B30017",
		// "B30018", "B40001", "B40002", "B40003", "B40004", "B40005", "B40006", "B40007", "B40008", "B40009",
		// "B40010", "B40011", "B40012", "B40013", "B40014", "B40015", "B40016", "B40017", "B40018", "B40019",
		// "B40020", "B40021", "B40022", "B40023", "B40024", "B40025", "B40026", "B40027", "B40028", "B40029",
		// "B40030", "B40031", "B40032", "B40033", "B40034", "B40035", "B40036", "B40037", "B40044", "B40045",
		// "B50001", "B50002", "B50003", "B50004", "B50005", "B50006", "B50007", "B50008", "B50009", "B50010",
		// "B50011", "B50012", "B50013", "B50014", "B50015", "B50016", "B50017" };
		// String[] names = { "FA-Computer Hardware Purchase", "FA-Computer Software Purchase",
		// "FA-Computer Software Consultant", "Project in Progress", "R&D expense", "FA-Furniture",
		// "Long-term Unamortized Expenses-Decoration", "FA-Transport Facilities", "FA-Communication Equipment",
		// "FA-Office Equipment", "Recruitment Fee", "Welfare expense", "Employee Apartment Rental",
		// "Service Fee-Outsourcing", "Other HR cost", "Sales Incentive Cost", "Liberty Club Cost",
		// "Marketing Promotion", "Entertainment-Front line", "Conference expense-Front line",
		// "Domestic Travel expense (exclude intercity travel)/-Front line", "Agent Incentive Cost",
		// "Mobile Phone Fee-Front line", "Print Cost-Front line", "Travel expense-downtown-Front line",
		// "Vehicle Cost-Front line", "Expressage-Front line", "Service Fee-Front line",
		// "Branch Special Promotion Project", "Coinsurance Policy Issue cost", "Conference expense",
		// "Domestic Travel (excl. downtown)", "International Travel", "Entertainment", "Union Fee Charge",
		// "Membership Fee", "IT Software Maintenance Fee", "IT Hardware Maintenance Fee",
		// "Non-Motor Property Insurance", "Other Incidentals", "Repair Fee", "Low Cost Consumable",
		// "IT Hardware Consumable", "Stationery", "Books & Newspapers Cost", "Recycling Fee", "Vehicle Tax",
		// "Motor Insurance", "Mobile Fee", "Travel expense-downtown", "Expressage", "Vehicle Use expense",
		// "Vehicle Repair expense", "Printing Cost (excl. policy issue)", "Office Rent", "Estate Service Fee",
		// "Electricity & Water expense", "Network Fee", "Fixed-line Telephone Fee", "Bank Settlement/Fee",
		// "Other Tax", "IA Platform Usage Fee", "Service Fee-Claim Management",
		// "Service Fee-Sales/UW Management", "Service Fee-Back office Management and Other",
		// "SMS Platform Usage", "Mobile Fee (New Branch Preparation)", "Opening Cost", "Damage Preventive Fee",
		// "Business Publicity Expense", "Advertising", "Consultant Fee-Other", "Consultant Fee-Law",
		// "Litigation Cost", "Internal Training expense", "External Training expense", "Consultant Fee-IT",
		// "Other", "Audit Cost/Fee", "Actuarial Cost/Fee", "Senior Management Liability Insurance",
		// "Entertainment (Hilton)", "Domestic Travel (New Branch preparation)",
		// "Entertainment (New Branch preparation)", "Travel-other", "Foreign Affairs Service Expense" };
		// String line =
		// "UPDATE T_EXPENSE_BASE SET BASE_NAME = (BASE_NAME || '/' || '{1}') WHERE BASE_TYPE = 'expense_type' AND BASE_ID = '{0}';";
		// for (int i = 0, l = Math.max(ids.length, names.length); i < l; i++) {
		// System.out.println(line.replace("{0}", ids[i]).replace("{1}", names[i].replace("&", "' || '&' || '")));
		// }
		// Object[] aa = new Object[] { "xxx", "yyy", new Date() };
		// JSONObject json = new JSONObject();
		// // json.setDatePattern("yyyy-MM-dd");
		// json.put("x", aa);
		// System.out.println(json);
		// byte b = -1;
		// int i = 2556;
		//
		// b = Byte.MAX_VALUE;
		// b = Byte.MIN_VALUE;
		// System.out.println((byte) b);
		// System.out.println(i);
		// try {
		// final ServerSocket server = new ServerSocket(10000);
		// final Runnable run = new Runnable() {
		// public void run() {
		// try {
		// Socket socket = server.accept();
		// InputStream in = socket.getInputStream();
		// int c = 0;
		// while ((c = in.read()) != -1) {
		// System.out.println(c);
		// }
		// server.close();
		// System.exit(0);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// };
		// new Thread(run).start();
		// Socket socket = new Socket("127.0.0.1", 10000);
		// OutputStream out = socket.getOutputStream();
		// out.write(1);
		// out.write(2);
		// out.write(0);
		// System.out.println(Integer.toHexString(-1));
		// System.out.println(Integer.toHexString(-128));
		// System.out.println(Integer.toHexString(128));
		// out.flush();
		// out.write(Byte.MIN_VALUE);
		// out.write(new byte[] { -1, 0, 5 });
		// out.write(3);
		// out.write("XXXX".getBytes());
		// out.flush();
		// out.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
