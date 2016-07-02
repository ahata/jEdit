import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Formatter

public class SignUtil {
	public static String sha1(File file) {
		sha1(file.bytes)
	}

	public static String sha1(byte[] content) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1")
			return byteToHex(md.digest(content))
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e)
		}
	}

	public static String sha1(String content) {
		return sha1(content.getBytes())
	}

	public final static String md5(File file) {
		md5(file.bytes)
	}

	public final static String md5(byte[] btInput) {
		def hexDigits = [ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' ]
		try {
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5")
			// 使用指定的字节更新摘要
			mdInst.update(btInput)
			// 获得密文
			def md5bytes = mdInst.digest()
			// 把密文转换成十六进制的字符串形式
			int j = md5bytes.length
			def str = new char[j * 2]
			int k = 0
			for (int i = 0; i < j; i++) {
				byte byte0 = md5bytes[i]
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]
				str[k++] = hexDigits[byte0 & 0xf]
			}
			return new String(str)
		} catch (Exception e) {
			e.printStackTrace()
			return null
		}
	}

	public final static String md5(String content) {
		return md5(content.getBytes())
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter()
		for (byte b : hash) {
			formatter.format("%02x", b)
		}
		String result = formatter.toString()
		formatter.close()
		return result
	}
}
