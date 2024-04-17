package com.wj.reggie.controller;

import com.wj.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author wj
 * @version 1.0
 */
//upload download
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
//file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会被删除
        // fileは一時ファイルであり、指定された場所に保存されないと、このリクエストが完了すると一時ファイルが削除される
        log.info(file.toString());
//        获得原始文件名,不推荐，因为原始文件可能有重名现象
        // オリジナルファイル名を取得する（推奨されていない、オリジナルファイル名に重複がある可能性があるため）
       String originalFilename=file.getOriginalFilename();
       //将原始文件名里的后缀拿出来
        // オリジナルファイル名から拡張子を取り出す

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用uuid重新生成文件名，防止文件名重复或者文件覆盖
        // ファイル名の重複やファイルの上書きを防ぐために、UUIDを使用してファイル名を再生成する
        String fileName = UUID.randomUUID().toString()+suffix;
// ディレクトリオブジェクトを作成する
        //创建一个目录对象
        File dir =new File(basePath);
        //判断当前目录是否存在
        // 現在のディレクトリが存在するかどうかを判断する
        if(!dir.exists()){
          //目录不存在，需要创建
            // ディレクトリが存在しない場合、作成する
            dir.mkdirs();
        }
        try {
            //临时文件转存到指定位置
            // 一時ファイルを指定された場所に保存する
            file.transferTo(new File(basePath+"/"+fileName));//转存的文件通过配置文件配置
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);//数据库需要这个文件名// データベースはこのファイル名が必要です
    }

    //这里的下载指的是在浏览器中再次显示，相当于加载
    // ここでのダウンロードは、ブラウザで再度表示することを指します
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //输入流，通过输入流读取文件内容
        // 入力ストリームを通じてファイルの内容を読み取る
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath +"/"+ name));

            //输出流，通过输出流将文件写会浏览器，在浏览器展示图片
            // 出力ストリームを通じてファイルをブラウザに書き込み、画像をブラウザに表示する
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len=0;
            byte[] bytes=new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush(); //将读取的数据刷新// 読み取ったデータをフラッシュする
            }
//关闭资源// リソースをクローズする
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }
}
