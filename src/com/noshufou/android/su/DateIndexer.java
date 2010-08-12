package com.noshufou.android.su;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import android.content.Context;
import android.database.Cursor;
import android.widget.SectionIndexer;

public class DateIndexer implements SectionIndexer {
//	private static final String TAG = "Su.DateIndexer";
	
	private Cursor mCursor;
	private int mColumnIndex;
	private int mSectionCount;
	private String[] mSections;
	private int[] mSectionDates;
	private int[] mSectionPositions;
	private SimpleDateFormat mIntFormat;
	
	DateIndexer(Context context, Cursor cursor, int sortedColumnIndex) {
		mCursor = cursor;
		mColumnIndex = sortedColumnIndex;
		mIntFormat = new SimpleDateFormat("yD");
		GregorianCalendar calendar = new GregorianCalendar();
		
		mCursor.moveToFirst();
		long firstDateLong = mCursor.getLong(mColumnIndex);
		mCursor.moveToLast();
		long lastDateLong = mCursor.getLong(mColumnIndex);

		int firstDateInt = Integer.parseInt(mIntFormat.format(firstDateLong));
		int lastDateInt = Integer.parseInt(mIntFormat.format(lastDateLong));
		mSectionCount = (firstDateInt - lastDateInt) + 1;

		mSections = new String[mSectionCount];
		mSectionDates = new int[mSectionCount];
		mSectionPositions = new int[mSectionCount];

		calendar.setTimeInMillis(firstDateLong);
		for (int i = 0; i < mSectionCount; i++) {
			mSections[i] = Util.formatDate(context, calendar.getTimeInMillis());
			mSectionDates[i] = Integer.parseInt(mIntFormat.format(calendar.getTime()));
			mSectionPositions[i] = -1;
			calendar.add(GregorianCalendar.DATE, -1);
		}
	}

	public int getPositionForSection(int section) {
		if (mCursor == null) {
			return 0;
		}
		
		if (section <= 0) {
			return 0;
		}
		
		if (section >= mSectionCount) {
			return mCursor.getCount();
		}
		
		if (mSectionPositions[section] > 0) {
			return mSectionPositions[section];
		}
		
		int start = 0;
		int end = mCursor.getCount();
		
		for (int i = section - 1; i > 0; i--) {
			if (mSectionPositions[i] > 0) {
				start = mSectionPositions[i];
				break;
			}
		}
		
		int savedCursorPos = mCursor.getPosition();
		long date;
		int dateInt;
		for (int i = start; i < end; i++) {
			if (mCursor.moveToPosition(i)) {
				date = mCursor.getLong(mColumnIndex);
				dateInt = Integer.parseInt(mIntFormat.format(date));
				if (mSectionDates[section] >= dateInt) {
					mSectionPositions[section] = i;
					return i;
				}
			}
		}
		mCursor.moveToPosition(savedCursorPos);
				
		return 0;
	}

	public int getSectionForPosition(int position) {
		int savedCursorPos = mCursor.getPosition();
		mCursor.moveToPosition(position);
		long date = mCursor.getLong(mColumnIndex);
		mCursor.moveToPosition(savedCursorPos);
		long today = System.currentTimeMillis();
		int dateInt = Integer.parseInt(mIntFormat.format(date));
		int todayInt = Integer.parseInt(mIntFormat.format(today));
		return todayInt - dateInt;
	}

	public Object[] getSections() {
		return mSections;
	}

}
