package com.learnvideo.esdrawvideo.render;

/**
 * Created by Chenwei on 2017/12/8.
 */

public interface IRender {

   void create();
    void draw();
    void prepareDraw(int parentWidth, int parentHeight);
}
