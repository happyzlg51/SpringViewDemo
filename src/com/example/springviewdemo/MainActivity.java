package com.example.springviewdemo;

import java.util.ArrayList;
import java.util.List;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnPageChangeListener,
		OnClickListener {

	SpringView springView;
	ViewPager viewPager;

	View view1;
	View view2;
	View view3;
	View view4;

	int currentPosition = 0;
	int endPosition;
	int beginPosition;

	TextView[] tvs = new TextView[4];
	private TextView tab_new, tab_hot, tab_free, tab_member;
	private float item_width;
	private int screenWidth;

	int indicatorColorId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initattrs();
		init();
	}

	private void init() {
		if (indicatorColorsId != 0) {
			indicatorColorArray = getResources().getIntArray(indicatorColorsId);
		}
		LayoutInflater inflater = LayoutInflater.from(this);
		view1 = inflater.inflate(R.layout.vp1, null);
		view2 = inflater.inflate(R.layout.vp2, null);
		view3 = inflater.inflate(R.layout.vp3, null);
		view4 = inflater.inflate(R.layout.vp4, null);
		springView = (SpringView) findViewById(R.id.sot_springview);
		springView.setIndicatorColor(getResources().getColor(indicatorColorId));
		viewPager = (ViewPager) findViewById(R.id.vPager);
		List<View> views = new ArrayList<View>();
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);
		viewPager.setOnPageChangeListener(this);
		viewPager.setAdapter(new SortViewPagerAdapter(views));
		tab_hot = (TextView) findViewById(R.id.tv_tab_1);
		tab_new = (TextView) findViewById(R.id.tv_tab_2);
		tab_free = (TextView) findViewById(R.id.tv_tab_3);
		tab_member = (TextView) findViewById(R.id.tv_tab_4);
		tvs[0] = tab_hot;
		tvs[1] = tab_new;
		tvs[2] = tab_free;
		tvs[3] = tab_member;
		tab_hot.setOnClickListener(this);
		tab_new.setOnClickListener(this);
		tab_free.setOnClickListener(this);
		tab_member.setOnClickListener(this);
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		item_width = screenWidth / 12 - 2.0f;
		updateTextColor();
		viewPager.setCurrentItem(0);
		tab_hot.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight,
					int oldBottom) {
				createPoints();
				tab_hot.removeOnLayoutChangeListener(this);
			}
		});
	}

	private float radiusMax;
	private float radiusMin;
	private float radiusOffset;
	private float acceleration = 0.5f;
	private float headMoveOffset = 0.6f;
	private float footMoveOffset = 1 - headMoveOffset;
	private ViewPager.OnPageChangeListener delegateListener;
	private int indicatorColorsId;
	private static final int INDICATOR_ANIM_DURATION = 3000;
	private ObjectAnimator indicatorColorAnim;
	private int[] indicatorColorArray;

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int position, float offset, int offsetPixels) {
		if (currentPosition == position) {
			endPosition = (int) (item_width * currentPosition + (int) (item_width * offset));
		}
		if (currentPosition == position + 1) {
			endPosition = (int) (item_width * currentPosition - (int) (item_width * (1 - offset)));
		}
		// Animation mAnimation = new TranslateAnimation(beginPosition,
		// endPosition, 0, 0);
		// mAnimation.setFillAfter(true);
		// mAnimation.setDuration(0);
		// buttomLine.startAnimation(mAnimation);
		beginPosition = endPosition;

		if (position < tvs.length - 1) {
			// radius
			float radiusOffsetHead = 0.5f;
			if (offset < radiusOffsetHead) {
				springView.getHeadPoint().setRadius(radiusMin);
			} else {
				springView.getHeadPoint().setRadius(
						((offset - radiusOffsetHead) / (1 - radiusOffsetHead)
								* radiusOffset + radiusMin));
			}
			float radiusOffsetFoot = 0.5f;
			if (offset < radiusOffsetFoot) {
				springView.getFootPoint().setRadius(
						(1 - offset / radiusOffsetFoot) * radiusOffset
								+ radiusMin);
			} else {
				springView.getFootPoint().setRadius(radiusMin);
			}

			// x
			float headX = 1f;
			if (offset < headMoveOffset) {
				float positionOffsetTemp = offset / headMoveOffset;
				headX = (float) ((Math.atan(positionOffsetTemp * acceleration
						* 2 - acceleration) + (Math.atan(acceleration))) / (2 * (Math
						.atan(acceleration))));
			}
			springView.getHeadPoint().setX(
					getTabX(position) - headX * getPositionDistance(position));
			float footX = 0f;
			if (offset > footMoveOffset) {
				float positionOffsetTemp = (offset - footMoveOffset)
						/ (1 - footMoveOffset);
				footX = (float) ((Math.atan(positionOffsetTemp * acceleration
						* 2 - acceleration) + (Math.atan(acceleration))) / (2 * (Math
						.atan(acceleration))));
			}
			springView.getFootPoint().setX(
					getTabX(position) - footX * getPositionDistance(position));

			// reset radius
			if (offset == 0) {
				springView.getHeadPoint().setRadius(radiusMax);
				springView.getFootPoint().setRadius(radiusMax);
			}
		} else {
			springView.getHeadPoint().setX(getTabX(position));
			springView.getFootPoint().setX(getTabX(position));
			springView.getHeadPoint().setRadius(radiusMax);
			springView.getFootPoint().setRadius(radiusMax);
		}

		if (indicatorColorsId != 0) {
			float length = (position + offset)
					/ viewPager.getAdapter().getCount();
			int progress = (int) (length * INDICATOR_ANIM_DURATION);
			seek(progress);
		}

		springView.postInvalidate();
		if (delegateListener != null) {
			delegateListener.onPageScrolled(position, offset, offsetPixels);
		}
	}

	@Override
	public void onPageSelected(int position) {
		currentPosition = position;
		beginPosition = (int) (position * item_width);
		updateTextColor();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_tab_1:

			currentPosition = 0;
			break;
		case R.id.tv_tab_2:

			currentPosition = 1;
			break;
		case R.id.tv_tab_3:
			currentPosition = 2;

			break;
		case R.id.tv_tab_4:
			currentPosition = 3;

			break;

		}

		updateTextColor();
		viewPager.setCurrentItem(currentPosition);
	}

	public void updateTextColor() {
		for (int i = 0; i < tvs.length; i++) {

			if (i == currentPosition) {
				tvs[i].setTextColor(Color.WHITE);
			} else {
				tvs[i].setTextColor(Color.BLACK);
			}
		}

	}

	private void createPoints() {
		View view = tvs[viewPager.getCurrentItem()];
		springView.getHeadPoint().setX(view.getX() + view.getWidth() / 2);
		springView.getHeadPoint().setY(view.getY() + view.getHeight() / 2);
		springView.getFootPoint().setX(view.getX() + view.getWidth() / 2);
		springView.getFootPoint().setY(view.getY() + view.getHeight() / 2);
		springView.animCreate();
	}

	public float getTabX(int position) {
		return tvs[position].getX() + tvs[position].getWidth() / 2;
	}

	public float getPositionDistance(int position) {
		// TODO Auto-generated method stub
		float tarX = tvs[position + 1].getX();
		float oriX = tvs[position].getX();
		return oriX - tarX;
	}

	public void seek(int seekTime) {
		// TODO Auto-generated method stub
		if (indicatorColorAnim == null) {
			createIndicatorColorAnim();
		}
		indicatorColorAnim.setCurrentPlayTime(seekTime);
	}

	private void createIndicatorColorAnim() {
		indicatorColorAnim = ObjectAnimator.ofInt(springView, "indicatorColor",
				indicatorColorArray);
		indicatorColorAnim.setEvaluator(new ArgbEvaluator());
		indicatorColorAnim.setDuration(INDICATOR_ANIM_DURATION);
	}

	private void initattrs() {
		// TODO Auto-generated method stub
		indicatorColorId = R.color.foot_text_press;
		radiusMax = getResources().getDimension(R.dimen.si_default_radius_max);
		radiusMin = getResources().getDimension(R.dimen.si_default_radius_min);
		if (indicatorColorsId != 0) {
			indicatorColorArray = getResources().getIntArray(indicatorColorsId);
		}
		radiusOffset = radiusMax - radiusMin;
	}
}
