package com.sinosoft.efiling.util;

import java.io.File;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sinosoft.efiling.hibernate.entity.Company;
import com.sinosoft.efiling.service.UserService;
import com.sinosoft.util.FileHelper;
import com.sinosoft.util.StringHelper;

/**
 * 系统设置.系统相关的一些常量都放在类中
 * 
 * @author AnywnYu
 */
public class SystemUtils {
	/** 菜单、权限等表中的systemCode */
	public static final String SYSTEM_CODE = "eFiling";
	/** 系统使用的编码 */
	public static final String ENCODING = "UTF-8";
	/** 使用的国际化资源名称,必须与struts2配置的struts.custom.i18n.resources一致 */
	public static final String DEFAULT_RESOURCE_BUNDLE_NAME = "i18n/ApplicationResources";
	/** 20.默认分页数量 */
	public static final int DEFAULT_MAX_RESULTS = 20;
	/** 200.默认的档案盒可装页数 */
	public static final int DEFAULT_FILE_BOX_CAPACITY = 200;

	/** 1.是 */
	public static final String YES = "1";
	/** 0.否 */
	public static final String NO = "0";
	/** 是否 */
	public static final Map<String, String> YES_NO = new LinkedHashMap<String, String>();

	static {
		YES_NO.put(SystemUtils.YES, "global.yes");
		YES_NO.put(SystemUtils.NO, "global.no");
	}

	/** 1.承保资料。FileType.fileModel="1" */
	public static final String FILE_MODEL_FILE = "1";
	/** 0.影像文件。FileType.fileModel="0" */
	public static final String FILE_MODEL_IMAGE = "0";
	/** 2.手动归档文件。FileType.fileModel="0" */
	public static final String FILE_MODEL_MANUAL = "2";
	/** 3.ePolicy文件。FileType.fileModel="3" */
	public static final String FILE_MODEL_E_POLICY = "3";
	/** 9.其他文件。FileType.fileModel="9" */
	public static final String FILE_MODEL_OTHER = "9";

	/** 单份文件上传时的档案编码e-Document */
	public static final String FILE_NO_IMAGE = "e-Document";

	/** 承保资料类型在PrpDCode表的CodeType */
	public static final String FILE_TYPE_CODE_TYPE = "DocmentManageType";
	/**
	 * 不允许手动更改的文档类型，此类文档在数据库中事先配置，与PrpDCode表中的承保资料codeCode对应
	 * 
	 * <pre>
	 * // 027 被保人身份证/组织机构代码证
	 * // 028 被委托人身份证
	 * // 040 投保单
	 * // 041 投保人身份证/组织机构代码证
	 * // 044 新被保险人身份证/组织机构代码证
	 * // 045 新投保人身份证/组织机构代码证
	 * // 050 原被保人身份证/组织机构代码证
	 * // 051 原投保人身份证/组织机构代码证
	 * // 053 投保人及被保险人身份证
	 * // 054 投保人及被保险人组织机构代码证
	 * // 056 投保人及被保险人身份证或组织机构代码证
	 * // 065 投保人及被保险人身份证件或投保人的组织机构代码证
	 * // 067 交强险投保单
	 * // 068 投保人身份证正反面/组织机构代码证
	 * // 069 被保人身份证正反面/组织机构代码证
	 * // 071 原投保人身份证正反面/组织机构代码证
	 * // 072 原被保人身份证正反面/组织机构代码证
	 * // 073 新投保人身份证正反面/组织机构代码证
	 * // 074 新被保险人身份证正反面/组织机构代码证
	 * </pre>
	 * */
	public static final String[] FILE_TYPE_FINAL_CODES = {};

	/** 201.投保人/被保险人身份证或组织机构代码证,在eFiling中仅视为投保人 */
	public static final String FILE_TYPE_APPLICANT = "201";
	/** 200.被保人，(承保系统中“201.投保人/被保险人身份证或组织机构代码证”在eFiling中分为201和200） */
	public static final String FILE_TYPE_INSURED = "200";
	/** 250 营业执照复印件 */
	public static final String FILE_TYPE_LICENSE_BUSINESS = "250";
	/** 251 税务登记证复印件 */
	public static final String FILE_TYPE_LICENSE_TAX = "251";

	/** 204.行驶证 */
	public static final String FILE_TYPE_VEHICLE_LICENSE = "204";
	/** 行驶证相关资料类型,prpDCode表的codecode */
	public static final String[] FILE_TYPE_VEHICLE_CODES = { FILE_TYPE_VEHICLE_LICENSE };
	/**
	 * 投保人相关资料类型,prpDCode表的codecode,应该包括税务登记证和营业执照
	 */
	public static final String[] FILE_TYPE_APPLICANT_CODES = { FILE_TYPE_APPLICANT, FILE_TYPE_LICENSE_BUSINESS,
			FILE_TYPE_LICENSE_TAX };
	/**
	 * 被保人相关资料类型,prpDCode表的codecode
	 */
	public static final String[] FILE_TYPE_INSURED_CODES = { FILE_TYPE_APPLICANT };

	/**
	 * 身份证/组织机构代码证资料类型,prpDCode表的codecode. 包括FILE_TYPE_APPLICANT_CODES和FILE_TYPE_INSURED_CODES
	 * 
	 * @see #FILE_TYPE_APPLICANT_CODES
	 * @see #FILE_TYPE_INSURED_CODES
	 */
	public static final String[] FILE_TYPE_CLIENT_CODES = new String[FILE_TYPE_APPLICANT_CODES.length
			+ FILE_TYPE_INSURED_CODES.length];
	static {
		System.arraycopy(FILE_TYPE_APPLICANT_CODES, 0, FILE_TYPE_CLIENT_CODES, 0, FILE_TYPE_APPLICANT_CODES.length);
		System.arraycopy(FILE_TYPE_INSURED_CODES, 0, FILE_TYPE_CLIENT_CODES, FILE_TYPE_APPLICANT_CODES.length,
				FILE_TYPE_INSURED_CODES.length);
	}

	/** 作废单证:资料类型为作废单证的投保人字段保存的数据 */
	public static final String APPLICANT_VISA_VOIDED = "作废单证";
	/** 遗失单证:资料类型为遗失单证:的投保人字段保存的数据 */
	public static final String APPLICANT_VISA_LOST = "遗失单证";

	/** 008.资料类型为保单 */
	public static final String FILE_TYPE_POLICY = "008";
	/** 009.资料类型为投保单 */
	public static final String FILE_TYPE_PROPOSAL = "009";
	/** 007.资料类型为批单 */
	public static final String FILE_TYPE_ENDOR = "007";
	/** 002.资料类型为作废单证 */
	public static final String FILE_TYPE_VISA_VOIDED = "002";
	/** 003.资料类型为遗失单证 */
	public static final String FILE_TYPE_VISA_LOST = "003";
	/** 单证类型的状态 */
	public static final Map<String, String> VISA_STATUS = new LinkedHashMap<String, String>();
	static {
		VISA_STATUS.put(FILE_TYPE_VISA_VOIDED, "file.type.visa.voided");
		VISA_STATUS.put(FILE_TYPE_VISA_LOST, "file.type.visa.lost");
	}

	/** 6.电子保单文件,包括电子批单/投保单等 */
	public static final String FILE_TYPE_EPOLICY = "006";
	/** 001.资料类型为封面,即没有投保单等带有二维码的资料,但是通过承保手工打一个带有二维码的封面出来进行扫描的资料类型 */
	public static final String FILE_TYPE_COVER = "001";

	/** 0.投保单/保单等资料上面没有资料类型,因此默认为0,此时需要自动根据业务号识别资料类型 */
	public static final String FILE_TYPE_EMPTY = "0";

	// /** 60.归档期限默认60天 */
	// public static final int DEFAULT_TIME_LIMIT = 60;

	/** 60.归档期限默认60天 */
	public static final int FILE_DEADLINE_DEFAULT = 60;

	/** 1.有效状态（适用于大多数有validStatus或status字段的数据） */
	public static final String STATUS_VALID = "1";
	/** 0.无效数据（适用于大多数有validStatus或status字段的数据） */
	public static final String STATUS_INVALID = "0";
	public static final Map<String, String> STATUSES = new LinkedHashMap<String, String>();
	static {
		STATUSES.put(STATUS_VALID, "global.status.1");
		STATUSES.put(STATUS_INVALID, "global.status.0");
	}

	/** 0.车险.险种分类 */
	public static final String RISK_TYPE_AUTO = "0";
	/** 1.非车险.险种分类 */
	public static final String RISK_TYPE_NON_AUTO = "1";

	/* 单证类型 */
	/** 保单号的长度 */
	public static final int POLICY_NO_LENGTH = 22;
	/** 7.批单 */
	public static final String DOCUMENT_TYPE_ENDOR = "7";
	/** 8.保单 */
	public static final String DOCUMENT_TYPE_POLICY = "8";
	/** 9.投保单 */
	public static final String DOCUMENT_TYPE_PROPOSAL = "9";
	/** V.单证 */
	public static final String DOCUMENT_TYPE_VISA = "V";

	/** 所有单证类型及对应的描述 */
	public static final Map<String, String> DOCUMENT_TYPES = new LinkedHashMap<String, String>();
	static {
		DOCUMENT_TYPES.put(DOCUMENT_TYPE_PROPOSAL, "file.document.type.9");
		DOCUMENT_TYPES.put(DOCUMENT_TYPE_POLICY, "file.document.type.8");
		DOCUMENT_TYPES.put(DOCUMENT_TYPE_ENDOR, "file.document.type.7");
		DOCUMENT_TYPES.put(DOCUMENT_TYPE_VISA, "file.document.type.v");
	}

	/** 文档类型对应的描述 */
	public static String getDocumentTypeDescription(String documentType) {
		return DOCUMENT_TYPES.get(documentType);
	}

	/* 归档状态 */
	/** 00.未归档 */
	public static final String DOCUMENT_STATUS_UNFILE = "00";
	/** 01.归档不齐 */
	public static final String DOCUMENT_STATUS_LACK = "01";
	/** 11.归档齐全 */
	public static final String DOCUMENT_STATUS_FILE = "11";
	/** 10.手动归档齐全 */
	public static final String DOCUMENT_STATUS_FILE_MANUAL = "10";
	/** 所有单证归档状态及对应的描述 */
	public static final Map<String, String> DOCUMENT_STATUS = new LinkedHashMap<String, String>();
	static {
		DOCUMENT_STATUS.put(DOCUMENT_STATUS_UNFILE, "file.document.status.00");
		DOCUMENT_STATUS.put(DOCUMENT_STATUS_LACK, "file.document.status.01");
		DOCUMENT_STATUS.put(DOCUMENT_STATUS_FILE, "file.document.status.11");
		DOCUMENT_STATUS.put(DOCUMENT_STATUS_FILE_MANUAL, "file.document.status.10");
	}

	/* 承保上传资料审核状态 */
	/** 0.未审核 */
	public static final String FILE_APPROVE_STATUS_UNAUDITED = "0";
	/** 1.审核通过 */
	public static final String FILE_APPROVE_STATUS_AUDITED = "1";
	/** 2.审核不通过 */
	public static final String FILE_APPROVE_STATUS_NOPASSED = "2";
	/** 所有单证归档状态及对应的描述 */
	public static final Map<String, String> FILE_APPROVE_STATUS = new LinkedHashMap<String, String>();
	static {
		FILE_APPROVE_STATUS.put(FILE_APPROVE_STATUS_UNAUDITED, "file.approve.status.0");
		FILE_APPROVE_STATUS.put(FILE_APPROVE_STATUS_AUDITED, "file.approve.status.1");
		FILE_APPROVE_STATUS.put(FILE_APPROVE_STATUS_NOPASSED, "file.approve.status.2");
	}

	/** 文档类型对应的描述 */
	public static String getDocumentStatusDescription(String documentStatus) {
		return DOCUMENT_STATUS.get(documentStatus);
	}

	/** 0.差缺 */
	public static final String DOCUMENT_FILE_STATUS_LACK = "0";
	/** 1.已归档 */
	public static final String DOCUMENT_FILE_STATUS_FILE = "1";
	// /** 10.已上传归档文件,但是差缺原件,仅适用于资料类型为原件的资料(fileType.signed='1') */
	// public static final String DOCUMENT_FILE_STATUS_LACK_PAPER = "10";
	/** D.禁用,可能是被其他文件覆盖 */
	public static final String DOCUMENT_FILE_STATUS_DISABLED = "D";
	/** 归档状态及对应的描述 */
	public static final Map<String, String> DOCUMENT_FILE_STATUS = new LinkedHashMap<String, String>();
	static {
		DOCUMENT_FILE_STATUS.put(DOCUMENT_FILE_STATUS_FILE, "file.status.1");
		DOCUMENT_FILE_STATUS.put(DOCUMENT_FILE_STATUS_LACK, "file.status.0");
		// DOCUMENT_FILE_STATUS.put(DOCUMENT_FILE_STATUS_LACK_PAPER, "file.status.10");
		DOCUMENT_FILE_STATUS.put(DOCUMENT_FILE_STATUS_DISABLED, "file.status.D");

	}

	/** 归档状态的描述 */
	public static String getDocumentFileStatusDescription(String documentFileStatus) {
		return DOCUMENT_FILE_STATUS.get(documentFileStatus);
	}

	/** 0.档案盒不可用 */
	public static final String FILE_BOX_STATUS_INVALID = "0";
	/** 1.档案盒可用 */
	public static final String FILE_BOX_STATUS_VALID = "1";

	/** 1.被借出 */
	public static final String FILE_LENT_YES = "1";
	/** 0.未借出 */
	public static final String FILE_LENT_NO = "0";

	/** 1.借阅中 */
	public static final String FILE_LENDING_STATUS_LENDING = "1";
	/** 2.部分归还 */
	public static final String FILE_LENDING_STATUS_RETURN_PART = "2";
	/** 0.全部归还 */
	public static final String FILE_LENDING_STATUS_RETURNED = "0";
	/** 001: 条码值对应的id, 009: 投保单对应的id */
	// public static final String[] HAVE_NOT_SHARE = { "001", "009" };
	/** F.电子档案系统使用的岗位编号前缀 */
	public static final String GRADE_PREFIX = "F";

	/** 1.最终的操作菜单Menu.flag=1 */
	public static final String ACTION_MENU_FLAG = "1";

	/** 1.节点为分公司机构centerflag=1 */
	public static final String COMPANY_FLAG = "1";

	/** 0.内部机构,comAttribute=0 */
	public static final String COMPANY_INTERNAL = "0";
	/** 1.外部机构,comAttribute=1 */
	public static final String COMPANY_EXTERNAL = "1";

	/** 0.核心投保的数据 */
	public static final String DOCUMENT_SOURCE_CORE = "0";
	/** 1.电商数据 */
	public static final String DOCUMENT_SOURCE_B2C = "1";

	/** 1.被保险人 */
	public static final String CLIENT_RELATION_INSURED = "1";
	/** 2.投保人 */
	public static final String CLIENT_RELATION_APPLICANT = "2";

	/**
	 * 2.投保人
	 * 
	 * @deprecated
	 * @see #CLIENT_RELATION_APPLICANT
	 */
	public static final String RELATION_APPLICANT_FLAG = "2";
	/**
	 * 1.被保险人
	 * 
	 * @deprecated
	 * @see #CLIENT_RELATION_INSURED
	 */
	public static final String RELATION_INSURED_FLAG = "1";
	/** 1.单子状态生效 */
	public static final String UNDER_FLAG_YES_1 = "1";
	/** 3.单子无需核保 */
	public static final String UNDER_FLAG_YES_3 = "3";
	/** 单子的状态没有生效 */
	public static final String UNDER_FLAG_NO = "0";

	/** 商业险和交强险 */
	public static final String COMBINEFLAG_COMBINE = "COMBINE";
	/** 商业险 */
	public static final String COMBINEFLAG_MOTOR = "MOTOR";
	/** 交强险 */
	public static final String COMBINEFLAG_MTPL = "MTPL";

	/** 车险的两个险种 */
	public static final String CAR_RISK_0508 = "0508";
	public static final String CAR_RISK_0501 = "0501";

	/** 车险险类05 */
	public static final String CLASS_CODE_MOTOR = "05";
	/** 0501商业险 */
	public static final String RISK_CODE_MOTOR_0501 = "0501";
	/** 0508交强险 */
	public static final String RISK_CODE_MOTOR_0508 = "0508";

	/** 99.大保单 */
	public static final String CLASS_CODE_COVER_NOTE = "99";

	/** 系统初始化,读取一些常量,这个初始化必须放在SpringUtils之后 */
	public static void initialize() {
	}

	/** user的session名 */
	public static final String USER_SESSION_NAME = "user";
	/** 登录机构的session名 */
	public static final String DEPARTMENT_SESSION_NAME = "department";
	/** 登录分公司的session名 */
	public static final String COMPANY_SESSION_NAME = "company";

	/** 是否已登录的session名，只要这个属性不为true，则在首页需要选择登录机构 */
	public static final String LOGINED_SESSION_NAME = "logined";

	/** 1: 个人客户 */
	public static final String CLIENT_TYPE_PERSON = "1";
	/** 2: 单位客户 */
	public static final String CLIENT_TYPE_UNIT = "2";

	/** 01: 身份证 */
	public static final String PAPER_TYPE_01 = "01";
	/** 03: 护照 */
	public static final String PAPER_TYPE_03 = "03";
	/** 04: 军官照 */
	public static final String PAPER_TYPE_04 = "04";
	/** 51: 工商登记号 */
	public static final String PAPER_TYPE_51 = "51";
	/** 52: 税务登记号 */
	public static final String PAPER_TYPE_52 = "52";
	/** 53: 组织机构代码证(注意，在承保系统中中，个人客户可以选择51作为组织机构代码证,单位客户的51表示工商登记号) */
	public static final String PAPER_TYPE_53 = "53";
	/** 99: 其他 */
	public static final String PAPER_TYPE_99 = "99";

	/*
	 * 58 VehicleCategory K41 微型普通客车 59 VehicleCategory K42 微型越野客车 54 VehicleCategory K31 小型普通客车 55 VehicleCategory K32
	 * 小型越野客车 53 VehicleCategory K27 中型专用客车 47 VehicleCategory K21 中型普通客车 48 VehicleCategory K22 中型双层客车 49
	 * VehicleCategory K23 中型卧铺客车 50 VehicleCategory K24 中型铰接客车 51 VehicleCategory K25 中型越野客车 46 VehicleCategory K17
	 * 大型专用客车 40 VehicleCategory K11 大型普通客车 41 VehicleCategory K12 大型双层客车 42 VehicleCategory K13 大型卧铺客车 43
	 * VehicleCategory K14 大型铰接客车 44 VehicleCategory K15 大型越野客车
	 */
	/** 定义微型和小型客车代码 */
	public static final String[] CAR_KIND_SMALL_BUS = { "K41", "K42", "K31", "K32", "K33", "K34", "K43" };
	/** 定义中型客车代码 */
	public static final String[] CAR_KIND_NEUTER_BUS = { "K27", "K21", "K22", "K23", "K24", "K25", "26" };
	/** 定义大型客车代码 */
	public static final String[] CAR_KIND_LARGE_BUS = { "K17", "K11", "K12", "K13", "K14", "K15", "K16" };

	/** 定义车子的营业非营业 8:非营业 9：营业 */
	public static final String CAR_USENATURE_8 = "8";
	/** 定义车子的营业非营业 8:非营业 9：营业 */
	public static final String CAR_USENATURE_9 = "9";
	// 定义差缺信息
	public static final String LACK_MESSAGE = "尚未归档,差缺全部资料";
	/** 自动单证审核 */
	public static final String APPROVE_AUTO_STATUS = "0";
	/** 自动单证审核按概率抽取的手工单证审核 */
	public static final String APPROVE_AUTO_RATE_STATUS = "2";
	/** 手工单证审核 */
	public static final String APPROVE_NOAUTO_STATUS = "1";

	/** 核保勾选必须上传资料的状态 1:表示核保勾选过后必须提交的核保资料 */
	public static final String UNDWRT_SORTED_STATUS = "1";

	/**
	 * 根据request初始化登录用户等session信息
	 * 
	 * @param request
	 */
	public static void initializeUserSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Principal userPrincipal = request.getUserPrincipal();
		if (userPrincipal == null || StringHelper.isEmpty(userPrincipal.getName())) {
			// session.invalidate();
			return;
		}
		String userName = userPrincipal.getName(); // "nasay";
		UserSessionEntity user = (UserSessionEntity) session.getAttribute(USER_SESSION_NAME);
		if (user == null || !userName.equals(user.getName())) {
			// 需要重新登录
			// session.invalidate();
			initializeUserSession(session, null);
			session = request.getSession();
			UserService userService = SpringUtils.getBean(UserService.class);
			user = userService.getSessionEntity(userName);
			initializeUserSession(session, user);
			user.setLocale(request.getLocale());
		}
	}

	/** 需要删除的session */
	public static final String[] SESSION_NAMES = { USER_SESSION_NAME, DEPARTMENT_SESSION_NAME, COMPANY_SESSION_NAME,
			"UserCode", "ComCode", "CenterCode" };

	/**
	 * 在session中注入user对象
	 * 
	 * @param session
	 * @param user
	 */
	public static void initializeUserSession(HttpSession session, UserSessionEntity user) {
		if (user == null) {
			// 删除所有session,注意,不调用 session.invalidate()
			for (String name : SESSION_NAMES) {
				session.removeAttribute(name);
			}
			return;
		}
		session.setAttribute(USER_SESSION_NAME, user); // 当前登录人员
		session.setAttribute("UserCode", user.getId());

		Company company = null;
		company = user == null ? null : user.getCurrentDepartment();
		session.setAttribute(DEPARTMENT_SESSION_NAME, company); // 当前登录部门
		session.setAttribute("ComCode", company == null ? null : company.getId());

		company = user == null ? null : user.getCurrentCompany();
		session.setAttribute(COMPANY_SESSION_NAME, company); // 当前登录机构
		session.setAttribute("CenterCode", company == null ? null : company.getId());

	}

	/** 系统根目录 */
	private static String SERVER_HOME;

	/**
	 * 通过context对象得到web项目在系统中的主目录
	 * 
	 * @param context
	 */
	public static void setServerHome(ServletContext context) {
		if (!StringHelper.isEmpty(SERVER_HOME)) {
			return;
		}
		SERVER_HOME = FileHelper.getFilePath(context.getRealPath("/"));
		if (!SERVER_HOME.endsWith(File.separator)) {
			SERVER_HOME += File.separator;
		}
	}

	/**
	 * 得到系统主目录
	 * 
	 * @return
	 */
	public static String getServerHome() {
		return SERVER_HOME;
	}

	/**
	 * 得到项目中path在系统中所处的绝对路径
	 * 
	 * @param path
	 * @return
	 */
	public static String getServerPath(String path) {
		path = StringHelper.trim(path);
		if (path.charAt(0) == File.separatorChar) path = path.substring(1);
		return SERVER_HOME + path;
	}
}
