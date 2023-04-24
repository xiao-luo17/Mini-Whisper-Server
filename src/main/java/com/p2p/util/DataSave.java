package com.p2p.util;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;

public class DataSave {
    //这里进行registerPassword数据的持久化存储，保存用户名到Password的映射
    //可以用json或者xml格式存储，因为不想导依赖包就直接自定义格式化和解析方法
    /**
     * 将map数据格式化并保存到txt文件中
     */
    public static void MapToTxT(Map<String,String> map){

        String fpname= "./data.txt";
        String vcontent;
        BufferedWriter bufferedWriter=null;
        try {
            //自动创建目录
            File file = new File(fpname);
            File file_dir = new File(file.getParent());
            if(!file_dir.exists()){
                file_dir.mkdirs();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osr = new OutputStreamWriter(fos,"UTF-8");//避免中文乱码
            bufferedWriter = new BufferedWriter(osr);

            for (Map.Entry<String,String> entry : map.entrySet()) {
                //   用户名:密码｜
                vcontent="";
                vcontent+= entry.getKey();
                vcontent+=":";
                vcontent+=entry.getValue();
                bufferedWriter.write(vcontent);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter!=null){
                    bufferedWriter.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 将txt数据解析并保存到静态的registerMap中
     */
    public static Hashtable<String,String> TxTToMap(){
        Hashtable<String,String> hashtable = new Hashtable<>();
        //--------------读取文本-------------//
        String fpath= "./data.txt";
        BufferedReader bufferedReader=null;
        try {
            File file = new File(fpath);

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis,"UTF-8");//避免中文乱码
            bufferedReader = new BufferedReader(isr);

            String str_line="";
            String[] temp;
            //逐行读取文本
            while ((str_line=bufferedReader.readLine())!=null){
                temp = str_line.split(":");
                hashtable.put(temp[0],temp[1]);
                str_line="";
            }
            //读取文件并执行业务
            //.... list
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader!=null){
                    bufferedReader.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hashtable;
    }
}
