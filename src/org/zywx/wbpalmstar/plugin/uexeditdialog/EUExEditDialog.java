package org.zywx.wbpalmstar.plugin.uexeditdialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
    private static final String BUNDLE_DATA = "data";
    private static final int MSG_OPEN = 5;
    private static final int MSG_CLOSE = 6;
    private static final int MSG_INSERT = 7;
    private static final int MSG_CLEAN_ALL = 8;
    private static final int MSG_GET_CONTENT = 9;
    private static final int MSG_CLEAN = 10;
    final String INVALID_CODE = null;

    private HashMap<Integer, EditText> viewMap = new HashMap<Integer, EditText>();

    public EUExEditDialog(Context context, EBrowserView inParent) {
        super(context, inParent);
    }

    public void open(String[] params) {
        if (params == null || params.length < 11) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_OPEN;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    /**
     * 实际形式:open(String opId,String x,String y,String w,String h,String
     * fontSize,String fontColor,String inputType,String inputHint,String
     * defaultText,String maxNum)
     *
     * @param params
     */
    public void openMsg(String[] params) {
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


    public String create(String[] params) {
        if(params.length < 1) {
            if (BDebug.DEBUG) {
                Log.i(TAG, "invalid params");
            }
            return INVALID_CODE;
        }
        int opId = 0;
        int x, y, w, h, fontSize, fontColor, inputType, maxNum;
        String inputHint, defaultText;

        try {
            JSONObject jsonObject = new JSONObject(params[0]);
            opId = jsonObject.optInt("id", getRandomId());
            x = jsonObject.getInt("x");
            y = jsonObject.getInt("y");
            w = jsonObject.getInt("width");
            h = jsonObject.getInt("height");
            fontSize = jsonObject.getInt("fontSize");
            fontColor = parseColor(jsonObject.getString("fontColor"));
            inputType = jsonObject.getInt("inputType");
            inputHint = jsonObject.getString("inputHint");
            defaultText = jsonObject.getString("defaultText");
            maxNum = jsonObject.optInt("maxNum", 0);
            if (viewMap.containsKey(opId)) {
                return INVALID_CODE;
            }
            openEditDialog(opId, x, y, w, h, fontSize, fontColor, inputType, inputHint, defaultText, maxNum);
            return String.valueOf(opId);

        } catch (JSONException e) {
            if (BDebug.DEBUG) {
                e.printStackTrace();
            }
        }
        return INVALID_CODE;
    }

    private int getRandomId() {
        return (int)(Math.random() * 100000);
    }

    public boolean close(String[] params) {
        int opId = 0;
        try {
            opId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            jsCallback(CALLBACK_CLOSE, opId, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        final int finalOpId = opId;
        EditText editText = viewMap.remove(finalOpId);
        if (editText != null) {
            hideSoftKeyboard(mContext, editText);
            removeViewFromCurrentWindow(editText);
            jsCallback(CALLBACK_CLOSE, finalOpId, EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
            return true;
        }
        return false;
    }

    public void insert(String[] params) {
        if (params == null || params.length < 2) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_INSERT;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    /**
     * 实际形式: insert(String opId,String text)
     *
     * @param params
     */
    public void insertMsg(String[] params) {
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
    }

    public String insertData(String[] params) {
        if(params.length < 1) {
            return INVALID_CODE;
        }
        int opId = 0;
        String text;
        try {
            JSONObject jsonObject = new JSONObject(params[0]);
            opId = jsonObject.optInt("id", getRandomId());
            text = jsonObject.getString("text");
        } catch (JSONException e) {
            return INVALID_CODE;
        }

        if (TextUtils.isEmpty(text)) {
            return INVALID_CODE;
        }
        EditText editText = viewMap.get(opId);
        if (editText != null) {
            Editable edit = editText.getEditableText();// 获取EditText的文字
            int maxLength = editText.getId();
            int appendLength = text.length();
            int index = editText.getSelectionStart();// 获取光标所在位置
            edit.insert(index, text);// 在光标所在位置插入文字
            // 如果添加文字加上现有的文字长度超过最大长度，则提示失败
            if (maxLength > 0 && (edit.length() + appendLength > maxLength)) {
                jsCallback(CALLBACK_INSERT, opId, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            } else {
                jsCallback(CALLBACK_INSERT, opId, EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
            }
            int currentLength = edit.length();
            String js = SCRIPT_HEADER + "if(" + ON_NUM + "){" + ON_NUM + "(" + opId + ","
                    + (maxLength - currentLength) + ");}";
            onCallback(js);
            return String.valueOf(opId);
        }
        return INVALID_CODE;
    }



    public boolean cleanAll(String[] params) {
        int opId = 0;
        try {
            opId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            jsCallback(CALLBACK_CLEAN_ALL, opId, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        final int finalOpId = opId;
        EditText editText = viewMap.get(finalOpId);
        if (editText != null) {
            editText.setText(null);
            jsCallback(CALLBACK_CLEAN_ALL, finalOpId, EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
            return true;
        }
        return false;
    }

    public String getContent(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return null;
        }
        int opId = 0;
        try {
            opId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        final int finalOpId = opId;
        EditText editText = viewMap.get(finalOpId);
        if (editText != null) {
            String content = editText.getText().toString();
            jsCallback(CALLBACK_GET_CONTENT, finalOpId, EUExCallback.F_C_TEXT, content);
            return content;
        }
        return null;
    }

    @Override
    protected boolean clean() {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_CLEAN;
        mHandler.sendMessage(msg);
        return true;
    }

    private void cleanMsg() {
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

    private void openEditDialog(final int opId, final int x, final int y, final int w, final int h, final int fontSize,
                                final int fontColor, final int inputType, final String inputHint, final String defaultText, final int maxNum) {
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
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxNum)});
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

    @Override
    public void onHandleMessage(Message message) {
        if (message == null) {
            return;
        }
        Bundle bundle = message.getData();
        switch (message.what) {
            case MSG_OPEN:
                openMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_INSERT:
                insertMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_CLEAN:
                cleanMsg();
                break;
            default:
                super.onHandleMessage(message);
        }
    }
}