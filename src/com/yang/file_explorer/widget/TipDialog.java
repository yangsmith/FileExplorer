package com.yang.file_explorer.widget;

import com.yang.file_explorer.R;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TipDialog {

	private Context mContext = null;

	private CharSequence mTitleText;

	private CharSequence mMessageText;

	private CharSequence mpositiveButtonText;

	private CharSequence mnegativeButtonText;

	private OnClickListener mPositiveButtonListener;

	private OnClickListener mNegativeButtonListener;

	private int mIconID;

	public TipDialog(Context context) {
		mContext = context;
	}

	public final Dialog builderDialog() {

		Dialog dialog = new Dialog(mContext, R.style.MyAlertDialog);
		LayoutInflater layoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.dialog, null);

		ImageView imageIcon = (ImageView) view.findViewById(R.id.icon);
		TextView titTextView = (TextView) view.findViewById(R.id.title);
		TextView messageTextView = (TextView) view.findViewById(R.id.message);
		LinearLayout buttonsLayout = (LinearLayout) view
				.findViewById(R.id.buttons);
		Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
		Button negativeButton = (Button) view.findViewById(R.id.negativeButton);

		dialog.addContentView(view, new ViewGroup.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		if (mIconID != 0) {
			// 图标
			imageIcon.setVisibility(View.VISIBLE);
			imageIcon.setImageResource(mIconID);
		}

		if (mTitleText != null) {
			// 标题
			titTextView.setVisibility(View.VISIBLE);
			titTextView.setText(mTitleText);
		}

		if (mMessageText != null) {
			// 内容
			messageTextView.setVisibility(View.VISIBLE);
			messageTextView.setText(mMessageText);
		}

		if (!TextUtils.isEmpty(mpositiveButtonText)) {
			buttonsLayout.setVisibility(View.VISIBLE);
			positiveButton.setVisibility(View.VISIBLE);
			positiveButton.setText(mpositiveButtonText);
			positiveButton.setOnClickListener(mPositiveButtonListener);
		}

		if (!TextUtils.isEmpty(mnegativeButtonText)) {
			buttonsLayout.setVisibility(View.VISIBLE);
			negativeButton.setVisibility(View.VISIBLE);
			negativeButton.setText(mnegativeButtonText);
			negativeButton.setOnClickListener(mNegativeButtonListener);
		}
		dialog.setContentView(view);
		return dialog;
	}

	public TipDialog setTitle(int titleId) {
		mTitleText = mContext.getText(titleId);
		return this;
	}

	public TipDialog setTitle(CharSequence title) {
		mTitleText = title;
		return this;
	}

	public TipDialog setMessage(int messageId) {
		mMessageText = mContext.getText(messageId);
		return this;
	}

	public TipDialog setMessage(CharSequence message) {
		mMessageText = message;
		return this;
	}

	public TipDialog setIcon(int iconId) {
		mIconID = iconId;
		return this;
	}

	public TipDialog setPositiveButton(int textId,
			final OnClickListener listener) {
		mpositiveButtonText = mContext.getText(textId);
		mPositiveButtonListener = listener;
		return this;
	}

	public TipDialog setPositiveButton(CharSequence text,
			final OnClickListener listener) {
		mpositiveButtonText = text;
		mPositiveButtonListener = listener;
		return this;
	}

	public TipDialog setNegativeButton(int textId,
			final OnClickListener listener) {
		mnegativeButtonText = mContext.getText(textId);
		mNegativeButtonListener = listener;
		return this;
	}

	public TipDialog setNegativeButton(CharSequence text,
			final OnClickListener listener) {
		mnegativeButtonText = text;
		mNegativeButtonListener = listener;
		return this;
	}

}
