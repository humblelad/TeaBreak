package burp;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ConfigUI extends JPanel {

    private BurpExtender burpExtender;
    private JLabel configLabel;
    private JFormattedTextField timeField;
    private JButton timerButton;
    private JLabel helpLabel;
    private JLabel errorLabel;
    private Thread teaBreakThread;

    private boolean timerStarted;

    public ConfigUI() {}

    public ConfigUI(String timer, BurpExtender burpExtender) {
        this.burpExtender = burpExtender;
        configLabel = new JLabel("Tea Break After: ");
        timerButton = new JButton("Start Timer");
        helpLabel = new JLabel("Time should be in \"hh:mm:ss\" format");
        errorLabel = new JLabel("Invalid time");
        errorLabel.setVisible(false);

        SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        DateFormatter dateFormatter = new DateFormatter(simpleDateFormat);
        timeField = new JFormattedTextField(dateFormatter);
        timeField.setText(validateTimePatternAndReturnTimeString(timer));
        setLayout(new MigLayout());

        setFont(helpLabel, 12);
        setFont(errorLabel, 14);
        this.add(configLabel);
        this.add(timeField);
        this.add(timerButton, "wrap");
        this.add(helpLabel, " span, wrap");
        this.add(errorLabel, " span, grow");

        timerButton.addActionListener(e -> timerButtonAction());
    }

    private void setFont(JLabel label, int fontSize) {
        Font labelFont = label.getFont();
        label.setFont(new Font(labelFont.getName(), Font.PLAIN, fontSize));
    }

    private void timerButtonAction() {
        if(timerStarted) {
            if(teaBreakThread != null && teaBreakThread.isAlive()) {
                interruptTeaBreakThread();
                timerButton.setText("Start Timer");
                timerStarted = false;
            }
        } else {
            if ("00:00:00".equals(timeField.getText())) {
                errorLabel.setVisible(true);
            } else {
                burpExtender.saveLastTime(timeField.getText());
                errorLabel.setVisible(false);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) timeField.getValue());
                calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                long totalSleepInMillis = ((calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60) + (calendar.get(Calendar.MINUTE) * 60) + calendar.get(Calendar.SECOND)) * 1000;
                notifyAfter(totalSleepInMillis);
                timerStarted = true;
                timerButton.setText("Stop Timer");
            }
        }
    }

    private void notifyAfter(long timeInMillis) {
        teaBreakThread = new Thread(() -> {
            try {
                Thread.sleep(timeInMillis);
                showPopup();
            } catch (InterruptedException e) {
                // suppress this exception
            }
        });
        teaBreakThread.start();
    }

    void interruptTeaBreakThread() {
        teaBreakThread.interrupt();
    }

    private void showPopup() {
        JDialog d = new JDialog(new JFrame() , "TEA BREAK", true);
        d.setLayout(new MigLayout());
        JButton b = new JButton ("OK");
        b.addActionListener (e -> {
            d.dispose();

        });
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resetUI();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                resetUI();
            }
        });
        d.add(new JLabel("Health is wealth. Take a break from work now!"), "span, wrap");
        d.add(b, "al center");
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
    }

    private void resetUI() {
        SwingUtilities.invokeLater(() -> {
            timerButton.setText("Start Timer");
            timerStarted = false;
        });
    }

    private String validateTimePatternAndReturnTimeString(String time) {
        SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("HH:mm:ss");
        try {
            simpleDateFormat.parse(time);
            return time;
        } catch (NullPointerException | ParseException e) {
            e.printStackTrace();
        }
        return "01:00:00";
    }
}
