package moocollege.cn.kugougouslidemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

/**
 * Created by zsd on 2017/8/24 16:31
 * desc:仿酷狗音乐侧滑效果
 */

public class KuGouSlideMenu extends HorizontalScrollView {


    //左边的菜单
    private View mMenuView;
    //主页的内容
    private View mContentView;
    private Context mContext;
    //菜单的宽度
    private int mMenuWidth;
    //当前是否打开 默认一进来是关闭的
    private boolean mMenuIsOpen = false;
    private GestureDetector mGestureDetector; // 系统自带的手势处理类
    private boolean mIntercept = false;

    public KuGouSlideMenu(Context context) {
        this(context, null);
    }

    public KuGouSlideMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KuGouSlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mGestureDetector = new GestureDetector(mContext, new GestureDetectorListener());
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.KuGouSlideMenu);
        float rightMargin = array.getDimension(R.styleable.KuGouSlideMenu_menuRightMargin, ScreenUtils.dip2px(mContext, 50));
        //菜单的宽度 = 屏幕的宽度-菜单离右边的距离
        mMenuWidth = (int) (ScreenUtils.getScreenWidth(mContext) - rightMargin);
        array.recycle();
    }


    /**
     * 手势处理的监听类
     */
    private class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 快速滑动
            // 向右快速滑动会是正的  +   向左快速滑动 是  -
            // 如果菜单是打开的   向右向左快速滑动都会回调这个方法
            if (mMenuIsOpen) {
                if (velocityX < 0) {
                    closeMenu();
                    return true;
                }
            } else {
                if (velocityX > 0) {
                    openMenu();
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 1.这个方法在整个布局xml解析完毕走这个方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取菜单和主页内容
        //我们在这里getChildAt(0)拿的是我们布局中的LinearLayout
        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        if (childCount != 2) {
            //抛运行时异常，只能放置两个子view
            throw new RuntimeException("You can only place two sub view");
        }
        //拿到我们的菜单布局
        mMenuView = container.getChildAt(0);
        //拿到我们的主页内容的布局
        mContentView = container.getChildAt(1);
        ViewGroup.LayoutParams layoutMenuParams = mMenuView.getLayoutParams();
        //指定菜单的宽度
        layoutMenuParams.width = mMenuWidth;
        //7.0
        mMenuView.setLayoutParams(layoutMenuParams);
        ViewGroup.LayoutParams layoutContentParams = mContentView.getLayoutParams();
        //指定内容的宽度 指定宽高后会重新摆放 在onLayout中
        layoutContentParams.width = ScreenUtils.getScreenWidth(mContext);
        mContentView.setLayoutParams(layoutContentParams);
    }

    //2 布局摆放 默认进来进来是关闭的
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 用来排放子布局的   等子View全部摆放完才能去滚动 我们一进来的时候默认是关闭菜单的
        //类比纵向的ScrollVew的来理解
        scrollTo(mMenuWidth, 0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIntercept = false;
        if (mMenuIsOpen) {
            float currentX = ev.getX();
            if (currentX > mMenuWidth) {
                //关闭菜单
                closeMenu();
                //子view不响应任何事件 拦截子view的触摸事件
                //如果返回true 代表会拦截子view的触摸事件，但是会相应自己的onTouch事件
                mIntercept = true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    //3 事件的拦截处理
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //获取手指滑动速率，获取手指滑动的速率，当期大于一定值就认为是快速滑动 ， GestureDetector（系统提供好的类）
        //当菜单打开的时候，手指触摸右边内容部分需要关闭菜单，还需要拦截事件（打开情况下点击内容页不会响应点击事件）
        //这里保证了手势处理类的调用
        //快速滑动了 下面的拦截事件就不要处理了、
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        //如果有拦截，则不执行自己的onTouch方法
        if (mIntercept){
            return true;
        }
        // 拦截处理事件
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                int currentScrollX = getScrollX();
                //在这里注意currentScrollX的变化，当我们默认关闭菜单的时候去拉动ScrollView，数值在不断的变小
                if (currentScrollX < mMenuWidth / 2) {
                    //打开菜单
                    openMenu();
                } else {
                    //关闭菜单
                    closeMenu();
                }
                //确保super.onTouchEvent不会执行 这里看super.onTouchEvent源码中的fling方法
                //和smoothScrollTo的源码
                return true;
        }
        return super.onTouchEvent(ev);
    }


    //4 处理主页内容的缩放，左边的缩放和透明度的调节 这就需要不断的获取当前的滑动位置
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //l是从mMuneWidth一直变化到0
        //计算梯度值
        float scale = 1f * l / mMenuWidth; //梯度从1逐渐变为0
        //右边的缩放 最小0.7f 最大是1
        float rightScale = 0.7f + 0.3f * scale;
        //设置主页内容的缩放，默认是中心点缩放
        //设置缩放的中心点
        ViewCompat.setPivotX(mContentView, 0);
        ViewCompat.setPivotY(mContentView, mContentView.getMeasuredHeight() / 2);
        ViewCompat.setScaleX(mContentView, rightScale);
        ViewCompat.setScaleY(mContentView, rightScale);

        //设置菜单的缩放和透明度 从半透明到完全透明 0.5f到1.0f
        float menuAlpha = 0.5f + (1 - scale) * 0.5f;
        ViewCompat.setAlpha(mMenuView, menuAlpha);
        //缩放处理
        float menuScale = 0.7f + (1 - scale) * 0.3f;
        ViewCompat.setScaleX(mMenuView, menuScale);
        ViewCompat.setScaleY(mMenuView, menuScale);
        //设置平移 l*0.7f
        ViewCompat.setTranslationX(mMenuView, 0.25f * l);


    }

    /**
     * 关闭菜单
     */
    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false;
    }

    /**
     * 打开菜单
     */
    private void openMenu() {
        smoothScrollTo(0, 0);
        mMenuIsOpen = true;
    }
}
