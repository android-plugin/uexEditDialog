package org.zywx.wbpalmstar.plugin.uexeditdialog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import android.app.ActivityGroup;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class EUExEditDialog extends EUExBase {
	public static final int INPUT_TYPE_NORMAL = 0;
	public static final int INPUT_TYPE_DIGITAL = 1;
	public static final int INPUT_TYPE_EMAIL = 2;
	public static final int INPUT_TYPE_URL = 3;
	public static final int INPUT_TYPE_PWD = 4;
	public static final String CALLBACK_OPEN = "uexEditDialog.cbOpen";
	public static final String CALLBACK_CLOSE = "uexEditDialog.cbClose";
	public static final String CALLBACK_INSERT = "uexEditDialog.cbInsert";
	public static final String CALLBACK_CLEAN_ALL = "uexEditDialog.cbCleanAll";
	public static final String CALLBACK_GET_CONTENT = "uexEditDialog.cbGetContent";
	public static final String ON_NUM = "uexEditDialog.onNum";
	public static final String TAG = "uexEditDialog";
	private ActivityGroup activityGroup;

	private HashMap<Integer, EditText> viewMap = new HashMap<Integer, EditText>();

	public EUExEditDialog(Context context, EBrowserView inParent) {
		super(context, inParent);
		activityGroup = (ActivityGroup) context;
	}

	/**
	 * 实际形式:open(String opId,String x,String y,String w,String h,String
	 * fontSize,String fontColor,String inputType,String inputHint,String
	 * defaultText,String maxNum)
	 * 
	 * @param params
	 */
	public void open(String[] params) {
		if (params.length != 11) {
			return;
		}
		int opId = 0;
		int x = 0;
		int y = 0;
		int w = 0;
		int h = 0;
		int fontSize = 0;
		int fontColor = 0;
		int inputType = INPUT_TYPE_NORMAL;
		int maxNum = 0;
		try {
			opId = Integer.parseInt(params[0]);
			x = Integer.parseInt(params[1]);
			y = Integer.parseInt(params[2]);
			w = Integer.parseInt(params[3]);
			h = Integer.parseInt(params[4]);
			fontSize = Integer.parseInt(params[5]);
			fontColor = parseColor(params[6]);
			inputType = Integer.parseInt(params[7]);
			maxNum = Integer.parseInt(params[10]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		openEditDialog(opId, x, y, w, h, fontSize, fontColor, inputType, params[8], params[9], maxNum);
	}

	/**
	 * 实际形式:close(String opId)
	 * 
	 * @param params
	 */
	public void close(String[] params) {
		if (params.length != 1) {
			return;
		}
		int opId = 0;
		try {
			opId = Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			jsCallback(CALLBACK_CLOSE, opId, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		final int finalOpId = opId;
		activityGroup.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				EditText editText = viewMap.remove(finalOpId);
				if (editText != null) {
					hideSoftKeyboard(mContext, editText);
					removeViewFromCurrentWindow(editText);
					jsCallback(CALLBACK_CLOSE, finalOpId, EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
				}
			}
		});
	}

	/**
	 * 实际形式: insert(String opId,String text)
	 * 
	 * @param params
	 */
	public void insert(String[] params) {
		if (params.length != 2) {
			return;
		}
		int opId = 0;
		try {
			opId = Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			jsCallback(CALLBACK_INSERT, opId, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		final int finalOpId = opId;
		final String appendText = params[1];
		if (appendText == null) {
			return;
		}
		activityGroup.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				EditText editText = viewMap.get(finalOpId);
				if (editText != null) {
					Editable edit = editText.getEditableText();// 获取EditText的文字
					int maxLength = editText.getId();
					int appendLength = appendText.length();
					int index = editText.getSelectionStart();// 获取光标所在位置
					edit.insert(index, appendText);// 在光标所在位置插入文字
					// 如果添加文字加上现有的文字长度超过最大长度，则提示失败
					if (maxLength > 0 && (edit.length() + appendLength > maxLength)) {
						jsCallback(CALLBACK_INSERT, finalOpId, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
					} else {
						jsCallback(CALLBACK_INSERT, finalOpId, EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
					}
					int currentLength = edit.length();
					String js = SCRIPT_HEADER + "if(" + ON_NUM + "){" + ON_NUM + "(" + finalOpId + ","
							+ (maxLength - currentLength) + ");}";
					onCallback(js);
				}
			}// end run
		});

	}

	/**
	 * 实际形式:cleanAll(String opId)
	 * 
	 * @param params
	 */
	public void cleanAll(String[] params) {
		if (params.length != 1) {
			return;
		}
		int opId = 0;
		try {
			opId = Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			jsCallback(CALLBACK_CLEAN_ALL, opId, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		final int finalOpId = opId;
		activityGroup.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				EditText editText = viewMap.get(finalOpId);
				if (editText != null) {
					editText.setText(null);
					jsCallback(CALLBACK_CLEAN_ALL, finalOpId, EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
				}
			}
		});

	}

	/**
	 * 实际形式:getContent(String opId)
	 * 
	 * @param params
	 */
	public void getContent(String[] params) {
		if (params.length != 1) {
			return;
		}
		int opId = 0;
		try {
			opId = Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		final int finalOpId = opId;
		activityGroup.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				EditText editText = viewMap.get(finalOpId);
				if (editText != null) {
					String content = editText.getText().toString();
					jsCallback(CALLBACK_GET_CONTENT, finalOpId, EUExCallback.F_C_TEXT, content);
				}
			}
		});

	}

	@Override
	protected boolean clean() {
		activityGroup.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Set<Integer> keySet = viewMap.keySet();
				Iterator<Integer> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					Integer key = iterator.next();
					EditText editText = viewMap.get(key);
					if (editText != null) {
						hideSoftKeyboard(mContext, editText);
						removeViewFromCurrentWindow(editText);
					}
					iterator.remove();
				}
			}
		});
		return true;
	}

	private void openEditDialog(final int opId, final int x, final int y, final int w, final int h, final int fontSize,
			final int fontColor, final int inputType, final String inputHint, final String defaultText, final int maxNum) {
		activityGroup.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (viewMap.get(opId) == null) {
					EditText editText = createEditText(fontSize, fontColor, inputType, inputHint, defaultText, maxNum);
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w, h);
					lp.leftMargin = x;
					lp.topMargin = y;
					addViewToCurrentWindow(editText, lp);
					viewMap.put(opId, editText);
					jsCallback(CALLBACK_OPEN, opId, EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
					if (maxNum > 0) {
						editText.addTextChangedListener(new TextWatcher() {

							@Override
							public void onTextChanged(CharSequence s, int start, int before, int count) {

							}

							@Override
							public void beforeTextChanged(CharSequence s, int start, int count, int after) {

							}

							@Override
							public void afterTextChanged(Editable s) {
								int currentLength = s.length();
								String js = SCRIPT_HEADER + "if(" + ON_NUM + "){" + ON_NUM + "(" + opId + ","
										+ (maxNum - currentLength) + ");}";
								onCallback(js);
							}

						});
					}
				} else {
					jsCallback(CALLBACK_OPEN, opId, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
				}
			}
		});

	}

	/**
	 * 创建EditText
	 * 
	 * @param fontSize
	 * @param fontColor
	 * @param inputType
	 * @param inputHint
	 * @param defaultText
	 * @param maxNum
	 * @return
	 */
	private EditText createEditText(int fontSize, int fontColor, int inputType, String inputHint, String defaultText,
			int maxNum) {
		EditText editText = new EditText(mContext);
		// 设置键盘类型
		switch (inputType) {
		case INPUT_TYPE_NORMAL:
			editText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
			break;
		case INPUT_TYPE_DIGITAL:
			editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
			break;
		case INPUT_TYPE_EMAIL:
			editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			break;
		case INPUT_TYPE_URL:
			editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_URI);
			break;
		case INPUT_TYPE_PWD:
			editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
			break;
		}
		editText.setGravity(Gravity.LEFT | Gravity.TOP);
		// 设置多行显示
		editText.setSingleLine(false);
		// 设置字体大小
		editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		// 设置字体颜色
		editText.setTextColor(fontColor);
		// 设置输入框背景色默认为透明
		editText.setBackgroundColor(Color.TRANSPARENT);
		// 设置最大允许输入的字符长度，相当于在XML中配置android:maxLength
		editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxNum) });
		editText.setId(maxNum);
		// 设置提示文字
		editText.setHint(inputHint);
		// 设置默认显示文字
		editText.setText(defaultText);
		return editText;
	}

	/**
	 * 解析颜色值，发生错误时返回黑色
	 * 
	 * @param inColor
	 * @return
	 */
	public static int parseColor(String inColor) {
		int reColor = 0xFF000000;
		try {
			if (inColor != null && inColor.length() != 0) {
				inColor = inColor.replace(" ", "");
				if (inColor.charAt(0) == 'r') { // rgba
					int start = inColor.indexOf('(') + 1;
					int off = inColor.indexOf(')');
					inColor = inColor.substring(start, off);
					String[] rgba = inColor.split(",");
					int r = Integer.parseInt(rgba[0]);
					int g = Integer.parseInt(rgba[1]);
					int b = Integer.parseInt(rgba[2]);
					int a = Integer.parseInt(rgba[3]);
					reColor = (a << 24) | (r << 16) | (g << 8) | b;
				} else { // #
					inColor = inColor.substring(1);
					if (3 == inColor.length()) {
						char[] t = new char[6];
						t[0] = inColor.charAt(0);
						t[1] = inColor.charAt(0);
						t[2] = inColor.charAt(1);
						t[3] = inColor.charAt(1);
						t[4] = inColor.charAt(2);
						t[5] = inColor.charAt(2);
						inColor = String.valueOf(t);
					} else if (6 == inColor.length()) {

					}
					long color = Long.parseLong(inColor, 16);
					reColor = (int) (color | 0x00000000ff000000);
				}
			}
		} catch (Exception e) {
			;
		}
		return reColor;
	}

	public static void hideSoftKeyboard(Context context, EditText editText) {
		editText.clearFocus();
		InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
}