package org.zimmob.zimlx.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by saul on 04-08-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */

class MultiSelectListPreference extends ListPreference {
    private String DEFAULT_SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
    private String separator;
    private boolean[] entryChecked;

    public MultiSelectListPreference(Context context) {
        super(context, null);
    }

    public MultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        entryChecked = new boolean[getEntries().length];
        separator = DEFAULT_SEPARATOR;
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        //super.onPrepareDialogBuilder(builder);
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        if (entries == null || entryValues == null || entries.length != entryValues.length) {
            throw new IllegalStateException(
                    "MultiSelectListPreference requires an entries array and entryValues" +
                            "array which are both the same length");
        }
        restoreCheckedEntries();
        DialogInterface.OnMultiChoiceClickListener listener = (dialog, which, val) -> entryChecked[which] = val;
        builder.setMultiChoiceItems(entries, entryChecked, listener);
    }

    private CharSequence[] unpack(CharSequence val) {
        if (val == null || val == "") {
            return new CharSequence[0];
        } else {
            return ((String) val).split(separator);
        }
    }

    private CharSequence[] getCheckedValues() {
        return unpack(getValue());
    }

    private void restoreCheckedEntries() {
        CharSequence[] entryValues = getEntryValues();

        CharSequence[] vals = unpack(getValue());

        if (vals != null) {
            List<CharSequence> valuesList = Arrays.asList(vals);
            for (int i = 0; i < entryValues.length; i++) {
                CharSequence entry = entryValues[i];
                entryChecked[i] = valuesList.contains(entry);
            }
        }
    }

    protected void onDialogClosed(boolean positiveResult) {
        List<CharSequence> values = new ArrayList<>();
        CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) {
            for (int i = 0; i < entryValues.length; i++) {
                if (entryChecked[i] == true) {
                    String val = (String) entryValues[i];
                    values.add(val);
                }
            }
            String value = join(values, separator);
            setSummary(prepareSummary(values));
            setValueAndEvent(value);
        }
    }

    private void setValueAndEvent(String value) {
        if (callChangeListener(unpack(value)))
            setValue(value);
    }

    private CharSequence prepareSummary(List<CharSequence> joined) {
        List<String> titles = new ArrayList<>();
        CharSequence[] entryTitle = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int ix = 0;
        for (CharSequence value : entryValues) {
            if (joined.contains(value)) {
                titles.add((String) entryTitle[ix]);
            }
            ix += 1;
        }
        return join(titles, ", ");
    }

    @Override
    protected Object onGetDefaultValue(TypedArray typedArray, int index) {
        return typedArray.getTextArray(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object rawDefaultValue) {
        String value;
        CharSequence[] defaultValue;
        if (rawDefaultValue == null)
            defaultValue = new CharSequence[0];
        else
            defaultValue = (CharSequence[]) rawDefaultValue;
        List<CharSequence> joined = Arrays.asList(defaultValue);
        String joinedDefaultValue = join(joined, separator);
        if (restoreValue)
            value = getPersistedString(joinedDefaultValue);
        else
            value = joinedDefaultValue;

        setSummary(prepareSummary(Arrays.asList(unpack(value))));
        setValueAndEvent(value);
    }

    private static String join(Iterable<?> iterable, String separator) {
        Iterator<?> oIter;
        if (iterable == null || (!(oIter = iterable.iterator()).hasNext()))
            return "";
        StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
        while (oIter.hasNext())
            oBuilder.append(separator).append(oIter.next());
        return oBuilder.toString();
    }
}
