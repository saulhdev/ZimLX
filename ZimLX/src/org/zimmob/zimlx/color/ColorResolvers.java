package org.zimmob.zimlx.color;

public class ColorResolvers implements ColorEngine.OnColorChangeListener {


    @Override
    public void onColorChange(String resolver, int color, int foregroundColor) {
        //notifyChanged();
    }

    class RGBColorResolver {
    }

    class ARGBColorResolver {
    }

    class SystemAccentResolver {

        /*public int resolveColor(){
            int color = ContextThemeWrapper(engine.context, android.R.style.Theme_DeviceDefault).getColorAccent();
            if (Utilities.isOnePlusStock()) {
                String propertyValue = Utilities.getSystemProperty("persist.sys.theme.accentcolor", "");
                if (!TextUtils.isEmpty(propertyValue)) {
                    if (!propertyValue.startsWith("#")){
                        propertyValue = "#$propertyValue";}
                    try {
                        color = Color.parseColor(propertyValue);
                    } catch (IllegalArgumentException ex) {
                    }
                }
            }
            return color;
        }

        public String getDisplayName(){
            return engine.context.getString(R.string.color_system_accent);
        }*/
    }
}
