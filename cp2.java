import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yhk
 * @ClassName: cp2
 * @description: TODO
 * @date 2019/7/2415:40
 */
public class cp2 {
    public static void main(String[] args) throws Exception{
        // 读取文件路径
        String filePath = "//assetid.txt";

        List<String> list=readFileTxt(filePath);
        //要copy的文件路径
        String copyFromUrl = "/home/cogent/warehouse";
        //copy指定路径
        String copyToUrl = "";

        for(int i=0;i<list.size();i++){
            String kk=copyFromUrl+"/"+list.get(i);
            copy(kk,copyToUrl,list.get(i));
        }

    }

    /**
     * 读取txt文本内容
     * @param txtUrl TXT路径
     * @param
     */
    public static List<String> readFileTxt(String txtUrl){
        BufferedReader read=null;
        BufferedReader br=null;
        List<String> list=new ArrayList<>();

        try {
            File file =new File(txtUrl);
            if(file.isFile()&&file.exists()){
                FileInputStream fkk=new FileInputStream(file);
                InputStreamReader kk=new InputStreamReader(fkk);
                BufferedReader reader=new BufferedReader(kk);
                br=new BufferedReader(reader);
                String readLine=null;
                //循环打印txt的每一行
                while((readLine=br.readLine())!=null){
                    //去空格
                    readLine=readLine.trim();
                    list.add(readLine);
                    System.out.println(readLine);
                }
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        } finally{
            if (br != null)
                try {
                    br.close();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            if (read != null)
                try {
                    read.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return list;

    }

    //复制方法
    public static void copy(String src, String des,String folder) throws Exception {
        //初始化文件复制
        File file1=new File(src);
        //把文件里面内容放进数组
        File[] fs=file1.listFiles();
        //初始化文件粘贴
        File file2=new File(des);
        //判断是否有这个文件有不管没有创建
        if(!file2.exists()){
            file2.mkdirs();
        }


        //遍历文件及文件夹
        if(fs!=null){
            String url = des.concat("/").concat(folder);
            File file3 = new File(url);
            file3.mkdirs();
            for (File f : fs) {
                if(f.isFile()){
                    //文件
                    fileCopy(f.getPath(),des+"/"+folder+"/"+f.getName()); //调用文件拷贝的方法
                }else if(f.isDirectory()){
                    //文件夹
                    copy(f.getPath(),des+"/"+f.getName(),folder);//继续调用复制方法      递归的地方,自己调用自己的方法,就可以复制文件夹的文件夹了
                }
            }
        }
    }

    /**
     * 文件复制的具体方法
     */
    private static void fileCopy(String src, String des) throws Exception {
        //io流固定格式
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(des));
        int i = -1;//记录获取长度
        byte[] bt = new byte[2014];//缓冲区
        while ((i = bis.read(bt))!=-1) {
            bos.write(bt, 0, i);
        }
        bis.close();
        bos.close();
        //关闭流
    }
}
