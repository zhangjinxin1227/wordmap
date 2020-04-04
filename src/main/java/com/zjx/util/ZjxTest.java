package com.zjx.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZjxTest {

    public static void main(String[] args) {
        long a = System.currentTimeMillis();
        //System.out.println(wordsNumber("D:\\qq接收文件\\英语文章.txt"));
        System.out.println(product("456", "785"));
        System.out.println(System.currentTimeMillis() - a + "毫秒");
    }

    public static Long wordsNumber(String path){
        //对路径文件进行解析
        Long words = 0L;
        try {
            long lineNumber = Files.lines(Paths.get(path)).count();
            //对于大的文件处理；定义每次读取10行
            Integer n = (int)(lineNumber/10) + ((lineNumber % 10) == 0 ? 0 : 1);
            for(int i = 0; i < n; i++) {
                Stream<String> lines = Files.lines(Paths.get(path));
                String article = lines.skip( (i * 10) ).limit(10).collect(Collectors.toList()).toString();
                List<String> list = Arrays.asList(article.split("[ !\\?',;\\.]+"));
                words += (list.size() - 2);//减2的原因是去除左边的 [  ，和右边的 ]
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }


    public static String product(String numberOne, String numberTwo){
        //将两个字符串转换成int[]数组
        int[] temp1 = new int[ numberOne.length() ];//存放numberOne
        int[] temp2 = new int[ numberTwo.length() ];//numberTwo
        int[] temp3 = new int[ numberOne.length() + numberTwo.length() + 1];
        int size1 = temp1.length;
        int size2 = temp2.length;
        for (int i = 0; i < size1; i++)
        {
            temp1[i] = numberOne.charAt( i )-48;
        }
        for (int i = 0; i < size1; i++)
        {
            temp2[i] = numberTwo.charAt( i )-48;
        }
        //两个temp1  temp2存放完成   2 5 2 5 2 5
        //接下来乘积部分             3 6 5 4 2 1
        for(int i = size1 - 1; i >= 0; i--){
            for (int j = size2 - 1; j >=0; j--){
                int ten = temp1[ i ] * temp2[ j ] / 10;//十位上的数
                int unit = temp1[ i ] * temp2[ j ] % 10;//个位上的数
                temp3[ i + j + 1 ] = temp3[ i + j + 1 ] + unit;
                temp3[ i + j ] = temp3[ i + j ] + ten;
            }
        }
        for (int i = temp3.length - 1; i > 0 ; i--)
        {
            int n = temp3[i] / 10;//进位
            int m = temp3[ i ] % 10;//本位
            temp3[i] = m;
            temp3[i - 1] = temp3[i - 1] + n;
        }
        String result = "";
        for (int i = temp3.length - 1; i >= 0; i--){
            result = temp3[i] + result;
        }

        return result;
    }

}


