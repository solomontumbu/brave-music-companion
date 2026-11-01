package com.example.bravemusiccompanion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;

/**
 * Minimal Android launcher for YouTube Music in Brave.
 * Does not embed, scrape, proxy, or manipulate YouTube Music content.
 * Brave's background playback in Custom Tabs is device- and version-dependent.
 */
public class MainActivity extends Activity {
    private static final String BRAVE_PACKAGE = "com.brave.browser";
    private static final Uri MUSIC_URL = Uri.parse("https://music.youtube.com/");
    private static final Uri PLAY_URL = Uri.parse("https://play.google.com/store/apps/details?id=com.brave.browser");
    private static final int BACKGROUND = Color.rgb(16, 21, 27);
    private static final int SURFACE = Color.rgb(26, 34, 42);
    private static final int GREEN = Color.rgb(90, 226, 181);
    private static final int WHITE = Color.rgb(244, 248, 249);
    private static final int MUTED = Color.rgb(159, 173, 183);

    private boolean launchedOnStart = false;
    private TextView availabilityText;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        makeHomeScreen();
        // Start in the compact Brave view, once per process, not on every resume.
        if (state == null && !launchedOnStart) {
            launchedOnStart = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing()) openMusicCompact();
            }, 350);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (availabilityText != null) {
            availabilityText.setText(isBraveInstalled()
                    ? "●  Brave detected · Ready to play"
                    : "●  Brave not installed");
            availabilityText.setTextColor(isBraveInstalled() ? GREEN : Color.rgb(255, 164, 141));
        }
    }

    private boolean isBraveInstalled() {
        try {
            getPackageManager().getPackageInfo(BRAVE_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ex) {
            return false;
        }
    }

    private void openMusicCompact() {
        if (!isBraveInstalled()) { showInstallDialog(); return; }
        try {
            // The package is explicitly Brave; Android's default browser is irrelevant.
            CustomTabColorSchemeParams colors = new CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(BACKGROUND)
                    .setNavigationBarColor(BACKGROUND)
                    .build();
            CustomTabsIntent tab = new CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(colors)
                    .setShowTitle(true)
                    .setUrlBarHidingEnabled(true)
                    .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                    .build();
            tab.intent.setPackage(BRAVE_PACKAGE);
            tab.launchUrl(this, MUSIC_URL);
        } catch (ActivityNotFoundException | SecurityException ex) {
            Toast.makeText(this, "Brave compact view unavailable. Opening regular Brave.", Toast.LENGTH_LONG).show();
            openMusicFullBrowser();
        } catch (Exception ex) {
            Toast.makeText(this, "Compact view failed. Trying Brave normally.", Toast.LENGTH_LONG).show();
            openMusicFullBrowser();
        }
    }

    private void openMusicFullBrowser() {
        if (!isBraveInstalled()) { showInstallDialog(); return; }
        Intent intent = new Intent(Intent.ACTION_VIEW, MUSIC_URL);
        intent.setPackage(BRAVE_PACKAGE);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException ex) {
            new AlertDialog.Builder(this)
                    .setTitle("Unable to launch Brave")
                    .setMessage("Please update Brave or check whether it is enabled in Android Settings.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void showInstallDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Brave Browser required")
                .setMessage("This app uses Brave to open YouTube Music. Install Brave, then return here to continue.")
                .setNegativeButton("Not now", null)
                .setPositiveButton("Install Brave", (dialog, which) -> openBraveStore())
                .show();
    }

    private void openBraveStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + BRAVE_PACKAGE)));
        } catch (ActivityNotFoundException ex) {
            startActivity(new Intent(Intent.ACTION_VIEW, PLAY_URL));
        }
    }

    private int dp(float n) { return (int) (getResources().getDisplayMetrics().density * n + 0.5f); }

    private GradientDrawable cardBackground(int fill, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(20));
        if (stroke != 0) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private Button button(String label, boolean primary, Runnable onTap) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? BACKGROUND : WHITE);
        b.setBackground(cardBackground(primary ? GREEN : SURFACE, primary ? 0 : 0xFF374450));
        b.setOnClickListener(v -> onTap.run());
        b.setMinimumHeight(dp(58));
        b.setPadding(dp(14), dp(12), dp(14), dp(12));
        return b;
    }

    private void addGap(LinearLayout root, int height) {
        View spacer = new View(this);
        root.addView(spacer, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private void makeHomeScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(26), dp(35), dp(26), dp(28));
        scroll.addView(root);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_launcher);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(icon, new LinearLayout.LayoutParams(dp(78), dp(78)));
        addGap(root, 24);

        TextView eyebrow = text("MUSIC COMPANION", 12, GREEN, true);
        root.addView(eyebrow);
        addGap(root, 9);
        TextView title = text("Your music.\nOne tap away.", 32, WHITE, true);
        root.addView(title);
        addGap(root, 13);
        TextView description = text("Open YouTube Music through Brave, with a compact, app-like view.", 15, MUTED, false);
        root.addView(description);
        addGap(root, 26);

        availabilityText = text("Checking Brave...", 13, GREEN, true);
        availabilityText.setPadding(dp(17), dp(16), dp(12), dp(16));
        availabilityText.setBackground(cardBackground(SURFACE, 0xFF35444B));
        root.addView(availabilityText, new LinearLayout.LayoutParams(-1, -2));
        addGap(root, 22);

        Button compact = button("♫   Open Music · Compact view", true, this::openMusicCompact);
        root.addView(compact, new LinearLayout.LayoutParams(-1, dp(62)));
        addGap(root, 12);
        Button full = button("↗   Open in regular Brave", false, this::openMusicFullBrowser);
        root.addView(full, new LinearLayout.LayoutParams(-1, dp(62)));
        addGap(root, 25);

        TextView noteTitle = text("BACKGROUND PLAYBACK TEST", 12, GREEN, true);
        root.addView(noteTitle);
        addGap(root, 10);
        TextView note = text(
                "1. Play a song in Compact view.\n" +
                "2. Press Home, then lock the screen.\n" +
                "3. If playback stops, come back and try Regular Brave.\n\n" +
                "The compact browser toolbar may remain visible. Brave and YouTube Music determine playback behavior.",
                14, MUTED, false);
        root.addView(note);
        addGap(root, 35);
        TextView footer = text("Unofficial launcher · Not affiliated with Brave or YouTube", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);

        setContentView(scroll);
    }
}
