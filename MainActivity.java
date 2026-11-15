package com.chatgpttrytrylab.finalpos;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private static final String POS_URL = "https://chatgpttrytry-lab.github.io/Final/";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String PREFS = "pos_printer";
    private static final String MAC_KEY = "printer_mac";
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private BluetoothSocket socket;
    private OutputStream output;
    private WebView webView;
    private final Object lock = new Object();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new PrinterBridge(), "AndroidPrinter");
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 30 &&
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 7);
        }
        if (Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 8);
        }
        webView.loadUrl(POS_URL);
    }

    private boolean hasConnectPermission() {
        return Build.VERSION.SDK_INT < 31 ||
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean connectInternal() throws Exception {
        if (!hasConnectPermission()) return false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) return false;
        String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(MAC_KEY, "");
        Set<BluetoothDevice> paired = adapter.getBondedDevices();
        BluetoothDevice chosen = null;
        if (!saved.isEmpty()) {
            for (BluetoothDevice d : paired) if (saved.equalsIgnoreCase(d.getAddress())) { chosen = d; break; }
        }
        if (chosen == null) {
            for (BluetoothDevice d : paired) {
                String n = d.getName();
                if (n != null && (n.toUpperCase().contains("MT580P") || n.toUpperCase().contains("MT-580P"))) {
                    chosen = d; break;
                }
            }
        }
        if (chosen == null) return false; // Pair the MT580P once in Android Bluetooth settings first.
        synchronized (lock) {
            closeSocket();
            BluetoothSocket candidate = chosen.createRfcommSocketToServiceRecord(SPP_UUID);
            candidate.connect();
            socket = candidate;
            output = candidate.getOutputStream();
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(MAC_KEY, chosen.getAddress()).apply();
        }
        return true;
    }

    private void closeSocket() {
        try { if (output != null) output.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        output = null; socket = null;
    }

    private boolean runConnect() {
        try {
            Future<Boolean> f = io.submit((Callable<Boolean>) this::connectInternal);
            return f.get(18, TimeUnit.SECONDS);
        } catch (Exception e) { synchronized (lock) { closeSocket(); } return false; }
    }

    public class PrinterBridge {
        @JavascriptInterface public boolean connect() { return runConnect(); }
        @JavascriptInterface public boolean disconnect() {
            synchronized (lock) { closeSocket(); }
            return true;
        }
        @JavascriptInterface public boolean print(String base64) {
            try {
                byte[] data = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                Future<Boolean> f = io.submit(() -> {
                    synchronized (lock) {
                        if (socket == null || !socket.isConnected() || output == null) {
                            if (!connectInternal()) return false;
                        }
                        output.write(data);
                        output.flush();
                        return true;
                    }
                });
                return f.get(30, TimeUnit.SECONDS);
            } catch (Exception e) { return false; }
        }
        @JavascriptInterface public String getSavedPrinterName() {
            if (!hasConnectPermission()) return "";
            try {
                String mac = getSharedPreferences(PREFS, MODE_PRIVATE).getString(MAC_KEY, "");
                BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
                if (a != null && !mac.isEmpty()) return a.getRemoteDevice(mac).getName();
            } catch (Exception ignored) {}
            return "";
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 8 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            io.execute(this::connectInternal);
        }
    }

    @Override public void onDestroy() {
        synchronized (lock) { closeSocket(); }
        io.shutdownNow();
        if (webView != null) { webView.removeJavascriptInterface("AndroidPrinter"); webView.destroy(); }
        super.onDestroy();
    }
}
