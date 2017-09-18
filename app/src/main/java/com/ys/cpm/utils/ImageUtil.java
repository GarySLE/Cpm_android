package com.ys.cpm.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ys on 2017/6/26.
 */

public class ImageUtil { //todo kotlin转换

    /**
     * 保存文件
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public static File saveFile(Context context, Bitmap bm, String fileName) throws IOException {
        String path = context.getExternalCacheDir().getAbsolutePath() + "/thumb/";
        File dirFile = new File(path);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }
        File imgFile = new File(path + fileName);
        FileOutputStream fos = new FileOutputStream(imgFile);
        bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        return imgFile;
    }
}
