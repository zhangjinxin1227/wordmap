package com.zjx.util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 流水号生成器
 *
 * @project
 * @version v1.0
 * @author
 * @createDate
 * @company
 * @since JDK 1.7
 *
 */
public class PIDUtil implements Runnable {

	private final static String STR_62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private final static int PIX_LEN = 36;
	private static volatile int PIX_1 = 0;
	private static volatile int PIX_2 = 0;
	private static volatile int PIX_3 = 0;
	private static volatile int PIX_4 = 0;

	private static final AtomicInteger ATOM_INT = new AtomicInteger(0);
	private static final int MAX_36 = 36 * 36 * 36 * 36;
	
	public final static Set<String> STRING_SET = new HashSet<>();
	public final static List<String> STRING_LIST = new ArrayList<>();

	/**
	 * 生成短时间内不会重复的长度为15位的字符串。<br/>
	 * 生成策略为获取系统时间戳转换为16进制字符串值，该字符串值为11位，去掉第一位，留后十位<br/>
	 * 并追加1位"0-z"的自增字符串.<br/>
	 * 再追加4位"0-z"随机字符串.<br/>
	 *
	 * @return
	 * @author
	 */
	public final static synchronized String getSN() {
		String timeStr = Long.toHexString(System.currentTimeMillis()).substring(1, 11);
		StringBuilder sb = new StringBuilder(15);// 创建一个StringBuilder
		sb.append(timeStr);// 先添加当前时间的毫秒值的16进制
		PIX_1++;
		if (PIX_1 == PIX_LEN) {
			PIX_1 = 0;
		}
		sb.append(getRandomChar(4));
		sb.append(STR_62.charAt(PIX_1));
		return sb.toString().toUpperCase();
	}

	/**
	 * 生成短时间内不会重复的长度为15位的字符串。<br/>
	 * 生成策略为获取自1970年1月1日零时零分零秒至当前时间的毫秒数的16进制字符串值，该字符串值为11位<br/>
	 * 并追加四位"0-z"的自增字符串.<br/>
	 * 如果系统时间设置为大于<b>2304-6-27 7:00:26<b/>的时间，将会报错！<br/>
	 * 由于系统返回的毫秒数与操作系统关系很大，所以本方法并不准确。本方法可以保证在系统返回的一个毫秒数内生成36的4次方个（1679616）ID不重复。
	 * <br/>
	 * 经过测试：该方法效率 比 create15_2 方法快一倍
	 * 
	 * @return 15位短时间不会重复的字符串。<br/>
	 * @author
	 * @since JDK1.6
	 */
	public final static synchronized String create15() {
		StringBuilder sb = new StringBuilder(15);// 创建一个StringBuilder
		sb.append(Long.toHexString(System.currentTimeMillis()));// 先添加当前时间的毫秒值的16进制
		PIX_4++;
		if (PIX_4 == PIX_LEN) {
			PIX_4 = 0;
			PIX_3++;
			if (PIX_3 == PIX_LEN) {
				PIX_3 = 0;
				PIX_2++;
				if (PIX_2 == PIX_LEN) {
					PIX_2 = 0;
					PIX_1++;
					if (PIX_1 == PIX_LEN) {
						PIX_1 = 0;
					}
				}
			}
		}
		return sb.append(STR_62.charAt(PIX_1)).append(STR_62.charAt(PIX_2)).append(STR_62.charAt(PIX_3)).append(STR_62.charAt(PIX_4)).toString();
	}

	/**
	 * JAVA获得0-9,a-z,A-Z范围的随机数
	 * 
	 * @param length
	 *            随机数长度
	 * @return String
	 */
	public static String getRandomChar(int length) {
		Random random = new Random();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buffer.append(STR_62.charAt(random.nextInt(PIX_LEN)));
		}
		return buffer.toString();
	}

	private final static String create15_2() {
		StringBuilder sb = new StringBuilder(15);
		sb.append(Long.toHexString(System.currentTimeMillis()));
		ATOM_INT.compareAndSet(MAX_36, 0);
		String str = longTo36(ATOM_INT.incrementAndGet());
		if (str.length() == 1) {
			sb.append("000").append(str);
		} else if (str.length() == 2) {
			sb.append("00").append(str);
		} else if (str.length() == 3) {
			sb.append("0").append(str);
		} else {
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * 10进制转任意进制
	 * 
	 * @param num
	 *            Long型值
	 * @param base
	 *            转换的进制
	 * @return 任意进制的字符形式
	 */
	private static final String ten2Any(long num, int base) {
		StringBuilder sb = new StringBuilder(7);
		while (num != 0) {
			sb.append(STR_62.charAt((int) (num % base)));
			num /= base;
		}
		return sb.reverse().toString();
	}

	/**
	 * 将一个Long 值 转换为 62进制
	 * 
	 * @param num
	 * @return
	 */
	public static final String longTo62(long num) {
		return ten2Any(num, 62);
	}

	private static final String longTo36(long num) {
		return ten2Any(num, 36);
	}

	@Override
	public void run() {

		int i = 0;
		 for (i = 0; i < 1; i++) {
			 System.out.println(Thread.currentThread().getName() + " " + i + "--" + getSN());
		 }
		
	}

	public static void main(String[] args) {

		PIDUtil rtt = new PIDUtil();
		for (int i = 1; i <= 10;  i++) {
			new Thread(rtt, "新线程"+i).start();
		}
//		new Thread(rtt, "新线程1").start();
//		new Thread(rtt, "新线程2").start();
//		new Thread(rtt, "新线程3").start();
//		new Thread(rtt, "新线程4").start();
//		new Thread(rtt, "新线程5").start();
//		new Thread(rtt, "新线程6").start();
//		new Thread(rtt, "新线程7").start();
//		new Thread(rtt, "新线程8").start();
//		new Thread(rtt, "新线程9").start();
//		new Thread(rtt, "新线程10").start();
//		new Thread(rtt, "新线程11").start();
//		new Thread(rtt, "新线程12").start();
//		new Thread(rtt, "新线程13").start();
//		new Thread(rtt, "新线程14").start();
//		new Thread(rtt, "新线程15").start();
//		new Thread(rtt, "新线程16").start();
//		new Thread(rtt, "新线程17").start();
//		new Thread(rtt, "新线程18").start();
//		new Thread(rtt, "新线程19").start();
//		new Thread(rtt, "新线程20").start();
	}

}
