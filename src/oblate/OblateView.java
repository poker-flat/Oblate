/*
 * OblateView.java
 */

package oblate;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.Robot;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.math.BigDecimal;

/**
 * The application's main frame.
 */
public class OblateView extends FrameView {

    public OblateView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = OblateApp.getApplication().getMainFrame();
            aboutBox = new OblateAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        OblateApp.getApplication().show(aboutBox);
    }

    public boolean checkAndSetNullRangeValues(String str) {
        if (str.equals("")) {
            this.nauticalMiles.setText("");
            this.miles.setText("");
            this.kilometers.setText("");
            this.yards.setText("");
            this.feet.setText("");
            this.meters.setText("");
            return true;
        }
        return false;
    }

    public boolean checkAndSetNullLatValue(String str) {
        if (str.equals("")) {
            this.ddLat.setText("");
            this.dLat.setText("");
            this.mLat.setText("");
            this.sLat.setText("");
            return true;
        }
        return false;
    }

    public boolean checkAndSetNullLonValue(String str) {
        if (str.equals("")) {
            this.ddLon.setText("");
            this.dLon.setText("");
            this.mLon.setText("");
            this.sLon.setText("");
            return true;
        }
        return false;
    }

    public boolean checkAndSetNullFLatValue(String str) {
        if (str.equals("")) {
            this.ddFLat.setText("");
            this.dFLat.setText("");
            this.mFLat.setText("");
            this.sFLat.setText("");
            return true;
        }
        return false;
    }

    public boolean checkAndSetNullFLonValue(String str) {
        if (str.equals("")) {
            this.ddFLon.setText("");
            this.dFLon.setText("");
            this.mFLon.setText("");
            this.sFLon.setText("");
            return true;
        }
        return false;
    }

    private BigDecimal[] getDMS(String dd) {
        BigDecimal[] ret = new BigDecimal[3]; // [degrees, minutes, seconds]
        BigDecimal f;
        boolean negate = false;
        try {
            f = new BigDecimal(dd);
            if (f.signum() == -1) {
                f = f.negate();
                negate = true;
            }
            ret[0] = new BigDecimal(f.intValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN);
            ret[1] = new BigDecimal(f.subtract(ret[0]).multiply(new BigDecimal(60)).intValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN);
            ret[2] = new BigDecimal(f.subtract(ret[0]).multiply(new BigDecimal(60)).subtract(ret[1]).multiply(new BigDecimal(60)).toString()).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            if (negate) {
                ret[0] = ret[0].negate();
            }
            return ret;
        }
        catch (NumberFormatException nfe) {
            this.statusMessageLabel.setText("Error converting DMS!");
            throw nfe;
        }
    }

    private BigDecimal getDD(String sdegrees, String sminutes, String sseconds) {
        BigDecimal degrees = new BigDecimal(0);
        BigDecimal minutes = new BigDecimal(0);
        BigDecimal seconds = new BigDecimal(0);
        BigDecimal dd;
        boolean negate = false;

        if (!sdegrees.equals("")) {
            degrees = new BigDecimal(sdegrees).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            if (degrees.signum() == -1) {
                negate = true;
                degrees = degrees.negate();
            }
        }
        if (!sminutes.equals("")) {
            minutes = new BigDecimal(sminutes).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            if (minutes.signum() == -1) {
                minutes = minutes.negate();
            }
        }
        if (!sseconds.equals("")) {
            seconds = new BigDecimal(sseconds.trim()).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            if (seconds.signum() == -1) {
                seconds = seconds.negate();
            }
        }

        dd = degrees.add(minutes.divide(new BigDecimal("60.0"), 8, BigDecimal.ROUND_HALF_EVEN))
                    .add(seconds.divide(new BigDecimal("3600.0"), 8, BigDecimal.ROUND_HALF_EVEN))
                    .setScale(8, BigDecimal.ROUND_HALF_EVEN);
        if (negate) {
            dd = dd.negate();
        }
        return dd;

    }
    
    private void nauticalMilesKey() {
        try {
            if (this.checkAndSetNullRangeValues(this.nauticalMiles.getText().trim())) {
                return;
            }
            BigDecimal f = new BigDecimal(this.nauticalMiles.getText().trim()).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            this.miles.setText(f.multiply(new BigDecimal("1.15077945")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.kilometers.setText(f.multiply(new BigDecimal("1.85200")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.yards.setText(f.multiply(new BigDecimal("2025.37183")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.feet.setText(f.multiply(new BigDecimal("6076.11549")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.meters.setText(f.multiply(new BigDecimal("1852.0")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Error! Malformed number.";
            this.miles.setText(err);
            this.kilometers.setText(err);
            this.yards.setText(err);
            this.feet.setText(err);
            this.meters.setText(err);
        }
    }

    private void milesKey() {
        try {
            if (this.checkAndSetNullRangeValues(this.miles.getText().trim())) {
                return;
            }
            BigDecimal f = new BigDecimal(this.miles.getText().trim()).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            this.nauticalMiles.setText(f.multiply(new BigDecimal("0.868976242")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.kilometers.setText(f.multiply(new BigDecimal("1.609344")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.yards.setText(f.multiply(new BigDecimal("1760.0")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.feet.setText(f.multiply(new BigDecimal("5280.0")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.meters.setText(f.multiply(new BigDecimal("1609.344")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Error! Malformed number.";
            this.nauticalMiles.setText(err);
            this.kilometers.setText(err);
            this.yards.setText(err);
            this.feet.setText(err);
            this.meters.setText(err);
        }
    }

    private void kilometersKey() {
        try {
            if (this.checkAndSetNullRangeValues(this.kilometers.getText().trim())) {
                return;
            }
            BigDecimal f = new BigDecimal(this.kilometers.getText().trim()).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            this.nauticalMiles.setText(f.multiply(new BigDecimal("0.539956803")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.miles.setText(f.multiply(new BigDecimal("0.621371192")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.yards.setText(f.multiply(new BigDecimal("1093.6133")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.feet.setText(f.multiply(new BigDecimal("3280.8399")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.meters.setText(f.multiply(new BigDecimal("1000.0")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Error! Malformed number.";
            this.nauticalMiles.setText(err);
            this.miles.setText(err);
            this.yards.setText(err);
            this.feet.setText(err);
            this.meters.setText(err);
        }
    }

    private void yardsKey() {
        try {
            if (this.checkAndSetNullRangeValues(this.yards.getText().trim())) {
                return;
            }
            BigDecimal f = new BigDecimal(this.yards.getText().trim()).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            this.nauticalMiles.setText(f.multiply(new BigDecimal("0.000493736501")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.miles.setText(f.multiply(new BigDecimal("0.000568181818")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.kilometers.setText(f.multiply(new BigDecimal("0.0009144")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.feet.setText(f.multiply(new BigDecimal("3.0")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.meters.setText(f.multiply(new BigDecimal("0.9144")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Error! Malformed number.";
            this.nauticalMiles.setText(err);
            this.miles.setText(err);
            this.kilometers.setText(err);
            this.feet.setText(err);
            this.meters.setText(err);
        }
    }

    private void feetKey() {
        try {
            if (this.checkAndSetNullRangeValues(this.feet.getText().trim())) {
                return;
            }
            BigDecimal f = new BigDecimal(this.feet.getText().trim()).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            this.nauticalMiles.setText(f.multiply(new BigDecimal("0.000164578834")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.miles.setText(f.multiply(new BigDecimal("0.000189393939")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.kilometers.setText(f.multiply(new BigDecimal("0.0003048")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.yards.setText(f.multiply(new BigDecimal("0.333333333")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.meters.setText(f.multiply(new BigDecimal("0.3048")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Error! Malformed number.";
            this.nauticalMiles.setText(err);
            this.miles.setText(err);
            this.kilometers.setText(err);
            this.yards.setText(err);
            this.meters.setText(err);
        }
    }

    private void metersKey() {
        try {
            if (this.checkAndSetNullRangeValues(this.meters.getText().trim())) {
                return;
            }
            BigDecimal f = new BigDecimal(this.meters.getText().trim()).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            this.nauticalMiles.setText(f.multiply(new BigDecimal("0.000539956803")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.miles.setText(f.multiply(new BigDecimal("0.000621371192")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.kilometers.setText(f.multiply(new BigDecimal("0.001")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.yards.setText(f.multiply(new BigDecimal("1.0936133")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
            this.feet.setText(f.multiply(new BigDecimal("3.2808399")).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Error! Malformed number.";
            this.nauticalMiles.setText(err);
            this.miles.setText(err);
            this.kilometers.setText(err);
            this.yards.setText(err);
            this.feet.setText(err);
        }
    }

    private void ddLatKey() {
        try {
            if (this.checkAndSetNullLatValue(this.ddLat.getText().trim())) {
                return;
            }

            BigDecimal dms[];
            dms = this.getDMS(this.ddLat.getText().trim());

            this.dLat.setText(dms[0].toString());
            this.mLat.setText(dms[1].toString());
            this.sLat.setText(dms[2].toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.dLat.setText("");
            this.mLat.setText("");
            this.sLat.setText(err);
        }
    }

    private void dLatKey() {
        try {
            if (this.dLat.getText().trim().equals("") &&
                this.mLat.getText().trim().equals("") &&
                this.sLat.getText().trim().equals("")) {
                    this.ddLat.setText("");
                    this.dLat.setText("");
                    this.mLat.setText("");
                    this.sLat.setText("");
            }

            this.ddLat.setText(this.getDD(this.dLat.getText().trim(),
                                          this.mLat.getText().trim(),
                                          this.sLat.getText().trim()).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.ddLat.setText(err);
        }
    }

    private void ddLonKey() {
        try {
            if (this.checkAndSetNullLonValue(this.ddLon.getText().trim())) {
                return;
            }
            BigDecimal dms[];
            dms = this.getDMS(this.ddLon.getText().trim());

            this.dLon.setText(dms[0].toString());
            this.mLon.setText(dms[1].toString());
            this.sLon.setText(dms[2].toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.dLon.setText("");
            this.mLon.setText("");
            this.sLon.setText(err);
        }
    }

    private void ddFLatKey() {
        try {
            if (this.checkAndSetNullLatValue(this.ddFLat.getText().trim())) {
                return;
            }
            BigDecimal dms[];
            dms = this.getDMS(this.ddFLat.getText().trim());

            this.dFLat.setText(dms[0].toString());
            this.mFLat.setText(dms[1].toString());
            this.sFLat.setText(dms[2].toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.dFLat.setText("");
            this.mFLat.setText("");
            this.sFLat.setText(err);
        }
    }

    private void ddFLonKey() {
        try {
            if (this.checkAndSetNullLatValue(this.ddFLon.getText().trim())) {
                return;
            }
            BigDecimal dms[];
            dms = this.getDMS(this.ddFLon.getText().trim());

            this.dFLon.setText(dms[0].toString());
            this.mFLon.setText(dms[1].toString());
            this.sFLon.setText(dms[2].toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.dFLon.setText("");
            this.mFLon.setText("");
            this.sFLon.setText(err);
        }
    }

    private void dLonKey() {
        try {
            if (this.dLon.getText().trim().equals("") &&
                this.mLon.getText().trim().equals("") &&
                this.sLon.getText().trim().equals("")) {
                    this.ddLon.setText("");
                    this.dLon.setText("");
                    this.mLon.setText("");
                    this.sLon.setText("");
            }

            this.ddLon.setText(this.getDD(this.dLon.getText().trim(),
                                          this.mLon.getText().trim(),
                                          this.sLon.getText().trim()).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.ddLon.setText(err);
        }
    }

    private void dFLatKey() {
        try {
            if (this.dFLat.getText().trim().equals("") &&
                this.mFLat.getText().trim().equals("") &&
                this.sFLat.getText().trim().equals("")) {
                    this.ddFLat.setText("");
                    this.dFLat.setText("");
                    this.mFLat.setText("");
                    this.sFLat.setText("");
            }

            this.ddFLat.setText(this.getDD(this.dFLat.getText().trim(),
                                           this.mFLat.getText().trim(),
                                           this.sFLat.getText().trim()).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.ddFLat.setText(err);
        }
    }

    private void dFLonKey() {
        try {
            if (this.dFLon.getText().trim().equals("") &&
                this.mFLon.getText().trim().equals("") &&
                this.sFLon.getText().trim().equals("")) {
                    this.ddFLon.setText("");
                    this.dFLon.setText("");
                    this.mFLon.setText("");
                    this.sFLon.setText("");
            }

            this.ddFLat.setText(this.getDD(this.dFLat.getText().trim(),
                                           this.mFLat.getText().trim(),
                                           this.sFLat.getText().trim()).toString());
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.ddFLon.setText(err);
        }
    }

    private void azimuthKey() {
        if (String.valueOf(this.azimuth.getText().trim()).equals("")) {
            this.azimuthErrorLabel.setText("");
            return;
        }
        try {
            Double az = Double.valueOf(this.azimuth.getText().trim()).doubleValue();
            this.azimuthErrorLabel.setText("");
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed number!";
            this.azimuthErrorLabel.setText(err);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        ddLat = new javax.swing.JTextField();
        ddLon = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        mLat = new javax.swing.JTextField();
        dLat = new javax.swing.JTextField();
        sLat = new javax.swing.JTextField();
        dLon = new javax.swing.JTextField();
        mLon = new javax.swing.JTextField();
        sLon = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        nauticalMiles = new javax.swing.JTextField();
        miles = new javax.swing.JTextField();
        kilometers = new javax.swing.JTextField();
        yards = new javax.swing.JTextField();
        feet = new javax.swing.JTextField();
        meters = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        ddFLat = new javax.swing.JTextField();
        ddFLon = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        mFLat = new javax.swing.JTextField();
        dFLat = new javax.swing.JTextField();
        sFLat = new javax.swing.JTextField();
        dFLon = new javax.swing.JTextField();
        mFLon = new javax.swing.JTextField();
        sFLon = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        azimuth = new javax.swing.JTextField();
        azimuthErrorLabel = new javax.swing.JLabel();
        finalPointButton = new javax.swing.JButton();
        rangeAzimuthButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mainPanel.setMaximumSize(new java.awt.Dimension(611, 526));
        mainPanel.setMinimumSize(new java.awt.Dimension(611, 526));
        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(oblate.OblateApp.class).getContext().getResourceMap(OblateView.class);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        ddLat.setText(resourceMap.getString("ddLat.text")); // NOI18N
        ddLat.setName("ddLat"); // NOI18N
        ddLat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ddLatKeyReleased(evt);
            }
        });

        ddLon.setText(resourceMap.getString("ddLon.text")); // NOI18N
        ddLon.setName("ddLon"); // NOI18N
        ddLon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ddLonKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ddLat, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
            .addComponent(ddLon, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(ddLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ddLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(6, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        mLat.setText(resourceMap.getString("mLat.text")); // NOI18N
        mLat.setName("mLat"); // NOI18N
        mLat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dLatKeyReleased(evt);
            }
        });

        dLat.setText(resourceMap.getString("dLat.text")); // NOI18N
        dLat.setName("dLat"); // NOI18N
        dLat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dLatKeyReleased(evt);
            }
        });

        sLat.setText(resourceMap.getString("sLat.text")); // NOI18N
        sLat.setName("sLat"); // NOI18N
        sLat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dLatKeyReleased(evt);
            }
        });

        dLon.setText(resourceMap.getString("dLon.text")); // NOI18N
        dLon.setName("dLon"); // NOI18N
        dLon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dLonKeyReleased(evt);
            }
        });

        mLon.setText(resourceMap.getString("mLon.text")); // NOI18N
        mLon.setName("mLon"); // NOI18N
        mLon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dLonKeyReleased(evt);
            }
        });

        sLon.setText(resourceMap.getString("sLon.text")); // NOI18N
        sLon.setName("sLon"); // NOI18N
        sLon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dLonKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dLon, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                    .addComponent(dLat, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mLon, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(mLat, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sLon)
                    .addComponent(sLat, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(6, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel3.border.titleFont"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel4.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel4.border.titleFont"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        nauticalMiles.setText(resourceMap.getString("nauticalMiles.text")); // NOI18N
        nauticalMiles.setName("nauticalMiles"); // NOI18N
        nauticalMiles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nauticalMilesKeyReleased(evt);
            }
        });

        miles.setText(resourceMap.getString("miles.text")); // NOI18N
        miles.setName("miles"); // NOI18N
        miles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                milesKeyReleased(evt);
            }
        });

        kilometers.setText(resourceMap.getString("kilometers.text")); // NOI18N
        kilometers.setName("kilometers"); // NOI18N
        kilometers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                kilometersKeyReleased(evt);
            }
        });

        yards.setText(resourceMap.getString("yards.text")); // NOI18N
        yards.setName("yards"); // NOI18N
        yards.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                yardsKeyReleased(evt);
            }
        });

        feet.setText(resourceMap.getString("feet.text")); // NOI18N
        feet.setName("feet"); // NOI18N
        feet.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                feetKeyReleased(evt);
            }
        });

        meters.setText(resourceMap.getString("meters.text")); // NOI18N
        meters.setName("meters"); // NOI18N
        meters.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                metersKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(kilometers, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                    .addComponent(miles, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                    .addComponent(nauticalMiles, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(meters, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(feet, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(yards, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {feet, kilometers, meters, miles, nauticalMiles, yards});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(nauticalMiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(miles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(kilometers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(meters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yards, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(feet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel5.border.titleFont"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel6.border.title"))); // NOI18N
        jPanel6.setName("jPanel6"); // NOI18N

        ddFLat.setText(resourceMap.getString("ddFLat.text")); // NOI18N
        ddFLat.setName("ddFLat"); // NOI18N
        ddFLat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ddFLatKeyReleased(evt);
            }
        });

        ddFLon.setText(resourceMap.getString("ddFLon.text")); // NOI18N
        ddFLon.setName("ddFLon"); // NOI18N
        ddFLon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ddFLonKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ddFLat, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
            .addComponent(ddFLon, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(ddFLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ddFLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel7.border.title"))); // NOI18N
        jPanel7.setName("jPanel7"); // NOI18N

        mFLat.setText(resourceMap.getString("mFLat.text")); // NOI18N
        mFLat.setName("mFLat"); // NOI18N
        mFLat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dFLatKeyReleased(evt);
            }
        });

        dFLat.setText(resourceMap.getString("dFLat.text")); // NOI18N
        dFLat.setName("dFLat"); // NOI18N
        dFLat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dFLatKeyReleased(evt);
            }
        });

        sFLat.setText(resourceMap.getString("sFLat.text")); // NOI18N
        sFLat.setName("sFLat"); // NOI18N
        sFLat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dFLatKeyReleased(evt);
            }
        });

        dFLon.setText(resourceMap.getString("dFLon.text")); // NOI18N
        dFLon.setName("dFLon"); // NOI18N
        dFLon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dFLonKeyReleased(evt);
            }
        });

        mFLon.setText(resourceMap.getString("mFLon.text")); // NOI18N
        mFLon.setName("mFLon"); // NOI18N
        mFLon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dFLonKeyReleased(evt);
            }
        });

        sFLon.setText(resourceMap.getString("sFLon.text")); // NOI18N
        sFLon.setName("sFLon"); // NOI18N
        sFLon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dFLonKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dFLon, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                    .addComponent(dFLat, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mFLon, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(mFLat, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sFLon)
                    .addComponent(sFLat, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dFLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mFLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sFLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dFLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mFLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sFLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel8.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel8.border.titleFont"))); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        azimuth.setText(resourceMap.getString("azimuth.text")); // NOI18N
        azimuth.setName("azimuth"); // NOI18N
        azimuth.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                azimuthFocusLost(evt);
            }
        });
        azimuth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                azimuthKeyReleased(evt);
            }
        });

        azimuthErrorLabel.setText(resourceMap.getString("azimuthErrorLabel.text")); // NOI18N
        azimuthErrorLabel.setName("azimuthErrorLabel"); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(azimuth, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(azimuthErrorLabel)
                .addContainerGap(314, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(azimuth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(azimuthErrorLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        finalPointButton.setText(resourceMap.getString("finalPointButton.text")); // NOI18N
        finalPointButton.setName("finalPointButton"); // NOI18N
        finalPointButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                finalPointButtonMouseReleased(evt);
            }
        });

        rangeAzimuthButton.setText(resourceMap.getString("rangeAzimuthButton.text")); // NOI18N
        rangeAzimuthButton.setName("rangeAzimuthButton"); // NOI18N
        rangeAzimuthButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rangeAzimuthButtonMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(finalPointButton, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(65, 65, 65)
                        .addComponent(rangeAzimuthButton, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, 0, 93, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(finalPointButton)
                    .addComponent(rangeAzimuthButton))
                .addGap(39, 39, 39))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(oblate.OblateApp.class).getContext().getActionMap(OblateView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 425, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void nauticalMilesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nauticalMilesKeyReleased
        this.nauticalMilesKey();
    }//GEN-LAST:event_nauticalMilesKeyReleased

    private void milesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_milesKeyReleased
        this.milesKey();
    }//GEN-LAST:event_milesKeyReleased

    private void kilometersKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kilometersKeyReleased
        this.kilometersKey();
    }//GEN-LAST:event_kilometersKeyReleased

    private void yardsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_yardsKeyReleased
        this.yardsKey();
    }//GEN-LAST:event_yardsKeyReleased

    private void ddLatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ddLatKeyReleased
        this.ddLatKey();
    }//GEN-LAST:event_ddLatKeyReleased

    private void dLatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dLatKeyReleased
        this.dLatKey();
    }//GEN-LAST:event_dLatKeyReleased

    private void feetKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_feetKeyReleased
        this.feetKey();
    }//GEN-LAST:event_feetKeyReleased

    private void metersKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_metersKeyReleased
        this.metersKey();
    }//GEN-LAST:event_metersKeyReleased

    private void ddLonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ddLonKeyReleased
        this.ddLonKey();
    }//GEN-LAST:event_ddLonKeyReleased

    private void ddFLatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ddFLatKeyReleased
        this.ddFLatKey();
    }//GEN-LAST:event_ddFLatKeyReleased

    private void ddFLonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ddFLonKeyReleased
        this.ddFLonKey();
    }//GEN-LAST:event_ddFLonKeyReleased

    private void dLonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dLonKeyReleased
        this.dLonKey();
    }//GEN-LAST:event_dLonKeyReleased

    private void dFLatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dFLatKeyReleased
        this.dFLatKey();
    }//GEN-LAST:event_dFLatKeyReleased

    private void dFLonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dFLonKeyReleased
        this.dFLonKey();
    }//GEN-LAST:event_dFLonKeyReleased

    private void azimuthKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_azimuthKeyReleased
        this.azimuthKey();
    }//GEN-LAST:event_azimuthKeyReleased

    private void finalPointButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_finalPointButtonMouseReleased
        try {
            double lon = Double.valueOf(this.ddLon.getText().trim()).doubleValue();
            double lat = Double.valueOf(this.ddLat.getText().trim()).doubleValue();
            double range = Double.valueOf(this.kilometers.getText().trim()).doubleValue();
            double az = Double.valueOf(this.azimuth.getText().trim()).doubleValue();

            Point wp = new Point(lon, lat);
            Point fin = wp.geoWaypoint(range, az);

            BigDecimal flat = new BigDecimal(String.valueOf(fin._y)).setScale(8, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal flon = new BigDecimal(String.valueOf(fin._x)).setScale(8, BigDecimal.ROUND_HALF_EVEN);
            this.ddFLat.setText(flat.toString());
            this.ddFLon.setText(flon.toString());

            this.ddFLatKey(); // takes care of all dms values
            this.ddFLonKey(); // takes care of all dms values
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed numbers. Could not calculate final point.";
            this.statusMessageLabel.setText(err);
            return;
        }
    }//GEN-LAST:event_finalPointButtonMouseReleased

    private void rangeAzimuthButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rangeAzimuthButtonMouseReleased
        try {
            double lon = Double.valueOf(this.ddLon.getText().trim()).doubleValue();
            double lat = Double.valueOf(this.ddLat.getText().trim()).doubleValue();
            double flon = Double.valueOf(this.ddFLon.getText().trim()).doubleValue();
            double flat = Double.valueOf(this.ddFLat.getText().trim()).doubleValue();

            Point initialPoint = new Point(lon, lat);
            Point finalPoint = new Point(flon, flat);
            
            BigDecimal az = new BigDecimal(String.valueOf(initialPoint.geoBearingTo(finalPoint))).setScale(4, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal km = new BigDecimal(String.valueOf(initialPoint.geoDistanceTo(finalPoint))).setScale(6, BigDecimal.ROUND_HALF_EVEN);
            this.azimuth.setText(az.toString());
            this.kilometers.setText(km.toString());

            this.azimuthKey();
            this.kilometersKey();
        }
        catch (NumberFormatException nfe) {
            String err = "Malformed numbers. Could not calculate range and azimuth.";
            this.statusMessageLabel.setText(err);
            return;
        }
    }//GEN-LAST:event_rangeAzimuthButtonMouseReleased

    private void azimuthFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_azimuthFocusLost
        try {
            BigDecimal az = new BigDecimal(this.azimuth.getText().trim()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
            this.azimuth.setText(az.toString());
        }
        catch (NumberFormatException nfe) {
            //
        }
    }//GEN-LAST:event_azimuthFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField azimuth;
    private javax.swing.JLabel azimuthErrorLabel;
    private javax.swing.JTextField dFLat;
    private javax.swing.JTextField dFLon;
    private javax.swing.JTextField dLat;
    private javax.swing.JTextField dLon;
    private javax.swing.JTextField ddFLat;
    private javax.swing.JTextField ddFLon;
    private javax.swing.JTextField ddLat;
    private javax.swing.JTextField ddLon;
    private javax.swing.JTextField feet;
    private javax.swing.JButton finalPointButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JTextField kilometers;
    private javax.swing.JTextField mFLat;
    private javax.swing.JTextField mFLon;
    private javax.swing.JTextField mLat;
    private javax.swing.JTextField mLon;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTextField meters;
    private javax.swing.JTextField miles;
    private javax.swing.JTextField nauticalMiles;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton rangeAzimuthButton;
    private javax.swing.JTextField sFLat;
    private javax.swing.JTextField sFLon;
    private javax.swing.JTextField sLat;
    private javax.swing.JTextField sLon;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextField yards;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
