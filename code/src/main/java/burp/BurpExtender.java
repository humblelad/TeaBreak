package burp;

import javax.swing.*;
import java.awt.*;

public class BurpExtender implements IBurpExtender, ITab, IExtensionStateListener {

    private static final String LAST_TIME_KEY = "last-tea-break-timer";
    private IBurpExtenderCallbacks callbacks;
    private ConfigUI configUI;

    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.callbacks.issueAlert("Tea break Extension Loaded Successfully.");
        String timer = this.callbacks.loadExtensionSetting(LAST_TIME_KEY);
        SwingUtilities.invokeLater(() -> callbacks.setExtensionName("Tea Break Extension"));
        configUI = new ConfigUI(timer, this);
        callbacks.addSuiteTab(this);
    }

    public String getTabCaption() {
        return "Tea Break";
    }

    public Component getUiComponent() {
        return configUI;
    }

    @Override
    public void extensionUnloaded() {
        configUI.interruptTeaBreakThread();
    }

    void saveLastTime(String time) {
        callbacks.saveExtensionSetting(LAST_TIME_KEY, time);
    }
}
