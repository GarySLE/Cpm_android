package com.ys.cpm.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;

/**
 * Created by Ys on 2017/6/22.
 * VideoUtil
 */

public class VideoUtil { //todo kotlin转换

    /**
     * @param videoPath 视频路径
     * @param width     图片宽度
     * @param height    图片高度
     * @param kind      eg:MediaStore.Video.Thumbnails.MICRO_KIND   MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        // 获取视频的缩略图
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        //extractThumbnail 方法二次处理,以指定的大小提取居中的图片,获取最终我们想要的图片
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 获取 raw 下 指定视频文件的缩略图
     *
     * @param resPath
     * @return
     */
    public static Bitmap getThumbnail(Context context, String resPath) {
        Uri videoURI = Uri.parse(resPath);
        return getThumbnail(context, videoURI);
    }

    private static Bitmap getThumbnail(Context context, Uri aVideoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, aVideoUri);
        return retriever
                .getFrameAtTime(1000 * 1000, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
    }
}
