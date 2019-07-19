package com.mmt.code;

import java.util.Arrays;

/**
 * 64位编码工具 自定义了编码字典表
 *
 * <b>这里的字典表请误随意改动</b>
 */
public class MMT64 {
    /**
     * 编码字典
     */
    private final static char[] ENCODE_TABLE = "hF13s_u52WRvIdxVDAKwNg0qEOoBP6YTnQjHyfZbSz7JaMXtiLGlcpr9U8emCk-4".toCharArray();
    /**
     * 解码字典
     */
    private final static byte[] DECODE_TABLE = new byte[256];

    /**
     * MD5附加字符串
     */
    private final static byte[] CHECK_SUM_SLAT = "#@com_mmt_md5%$".getBytes();

    static {
        Arrays.fill(DECODE_TABLE, (byte) -1);
        for (int i = 0; i < ENCODE_TABLE.length; i++) {
            DECODE_TABLE[ENCODE_TABLE[i]] = (byte) i;
        }
    }

    /**
     * 编码byte数组
     *
     * @param bytes
     *            数组
     * @return 编码字符串
     */
    public static String encode(byte[] bytes) {
        if (bytes == null) {
            // null被编码为“”
            return "";
        }
        if (bytes.length == 0) {
            // 0长度被编码为“h”
            return new String(new char[] { ENCODE_TABLE[0] });
        }
        // 3个byte为一组，此为完整分组数
        int fullGroups = bytes.length / 3;
        // 完整分组后剩下的
        int remainBytes = bytes.length % 3;
        char[] buf = new char[(bytes.length * 4 - 1) / 3 + 1];
        int i = 0;
        for (; i < fullGroups; i++) {
            int b0 = 255 & bytes[i * 3];
            int b1 = 255 & bytes[i * 3 + 1];
            int b2 = 255 & bytes[i * 3 + 2];
            int c0 = i * 4;
            int c1 = i * 4 + 1;
            int c2 = i * 4 + 2;
            int c3 = i * 4 + 3;
            buf[c0] = ENCODE_TABLE[encrypt(c0, b0 >>> 2)];
            buf[c1] = ENCODE_TABLE[encrypt(c1, ((3 & b0) << 4) | (b1 >>> 4))];
            buf[c2] = ENCODE_TABLE[encrypt(c2, ((15 & b1) << 2) | (b2 >>> 6))];
            buf[c3] = ENCODE_TABLE[encrypt(c3, 63 & b2)];
        }
        switch (remainBytes) {
            case 2:
                int b0 = 255 & bytes[i * 3];
                int b1 = 255 & bytes[i * 3 + 1];
                int c0 = i * 4;
                int c1 = i * 4 + 1;
                int c2 = i * 4 + 2;
                buf[c0] = ENCODE_TABLE[encrypt(c0, b0 >>> 2)];
                buf[c1] = ENCODE_TABLE[encrypt(c1, ((3 & b0) << 4) | (b1 >>> 4))];
                buf[c2] = ENCODE_TABLE[encrypt(c2, ((15 & b1) << 2))];
                break;
            case 1:
                b0 = 255 & bytes[i * 3];
                c0 = i * 4;
                c1 = i * 4 + 1;
                buf[c0] = ENCODE_TABLE[encrypt(c0, b0 >>> 2)];
                buf[c1] = ENCODE_TABLE[encrypt(c1, ((3 & b0) << 4))];
                break;
        }
        return new String(buf);
    }

    /**
     * 解码byte数组,非法数据会抛出运行期异常，调用程序需自行处理
     *
     * @param str
     *            编码的字符串
     * @return 对应byte数组
     */
    public static byte[] decode(String str) {
        if (str == null || str.length() == 0) {
            // null或empty被解码为null
            return null;
        }
        // 一个长度被解码为byte[0]
        if (str.length() == 1) {
            return new byte[0];
        }
        char[] chars = str.toCharArray();
        // 4个char为一组，此为完整分组数
        int fullGroups = chars.length / 4;
        // 完整分组后剩下的
        int remainChars = chars.length % 4;
        byte[] ret = new byte[(chars.length - 1) * 3 / 4 + 1];
        int i = 0;
        for (; i < fullGroups; i++) {
            int c0 = i * 4;
            int c1 = i * 4 + 1;
            int c2 = i * 4 + 2;
            int c3 = i * 4 + 3;
            int b0 = decrypt(c0, DECODE_TABLE[chars[c0]]);
            int b1 = decrypt(c1, DECODE_TABLE[chars[c1]]);
            int b2 = decrypt(c2, DECODE_TABLE[chars[c2]]);
            int b3 = decrypt(c3, DECODE_TABLE[chars[c3]]);
            ret[i * 3] = (byte) ((b0 << 2) | (b1 >>> 4));
            ret[i * 3 + 1] = (byte) ((b1 << 4) | (b2 >>> 2));
            ret[i * 3 + 2] = (byte) ((b2 << 6) | b3);
        }
        switch (remainChars) {
            case 3:
                int c0 = i * 4;
                int c1 = i * 4 + 1;
                int c2 = i * 4 + 2;
                int b0 = decrypt(c0, DECODE_TABLE[chars[c0]]);
                int b1 = decrypt(c1, DECODE_TABLE[chars[c1]]);
                int b2 = decrypt(c2, DECODE_TABLE[chars[c2]]);
                ret[i * 3] = (byte) ((b0 << 2) | (b1 >>> 4));
                ret[i * 3 + 1] = (byte) ((b1 << 4) | (b2 >>> 2));
                break;
            case 2:
                c0 = i * 4;
                c1 = i * 4 + 1;
                b0 = decrypt(c0, DECODE_TABLE[chars[c0]]);
                b1 = decrypt(c1, DECODE_TABLE[chars[c1]]);
                ret[i * 3] = (byte) ((b0 << 2) | (b1 >>> 4));
                break;
        }
        return ret;
    }

    /**
     * 加密
     *
     * @param charIndex
     *            字符序
     * @param tableIndex
     *            字典序
     * @return 混淆后的字典序
     */
    private static int encrypt(int charIndex, int tableIndex) {
        return (charIndex + tableIndex) % 64;
    }

    /**
     * 解密
     *
     * @param charIndex
     *            字符序
     * @param crptyIndex
     *            混淆后的字典序
     * @return 源字典序
     */
    private static int decrypt(int charIndex, int crptyIndex) {
        int r = crptyIndex - charIndex % 64;
        if (r < 0) {
            r += 64;
        }
        return r;
    }

    /**
     * 快速生成校验码
     *
     * @param content
     *            内容
     * @return 校验码
     */
    public static String checksumFast(byte[] content) {
        int h = 0;
        if (content != null) {
            for (int i = 0; i < content.length; i++) {
                h = 31 * h + content[i];
            }
        }
        if (CHECK_SUM_SLAT != null) {
            for (int i = 0; i < CHECK_SUM_SLAT.length; i++) {
                h = 31 * h + CHECK_SUM_SLAT[i];
            }
        }
        byte[] r = new byte[4];
        r[0] = (byte) (h >>> 24);
        r[1] = (byte) (h & 0x00ff0000 >>> 16);
        r[2] = (byte) (h & 0x0000ff00 >>> 8);
        r[3] = (byte) (h & 0x000000ff);
        return encode(r);
    }

    public static void main(String[] args) {
        String txt = "adsbaasdasdfasdfasd";
        String encodeStr = MMT64.encode(txt.getBytes());
        System.out.println(encodeStr);
        String decodeStr = new String(MMT64.decode(encodeStr));
        System.out.println(decodeStr);
        System.out.println(txt.equals(decodeStr));
        System.out.println(MMT64.checksumFast(txt.getBytes()));
    }
}
