package Luxora.Luxora;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

/**
 * Utility that attaches programmatic hover (mouse-enter/exit) effects to every
 * interactive Button found in a scene-graph subtree.
 *
 * Because JavaFX inline styles have higher specificity than CSS pseudo-classes,
 * hover styling must be applied programmatically for buttons that use inline styles.
 */
public class HoverUtils {

    /**
     * Recursively traverses {@code node} and adds hover effects to every
     * non-mouseTransparent Button whose style matches a known pattern.
     */
    public static void apply(Node node) {
        if (node == null) return;
        if (node instanceof Button btn) {
            if (!btn.isMouseTransparent()) attachHover(btn);
            return; // Button is a Parent (via Labeled) — no need to recurse further
        }
        if (node instanceof ScrollPane sp) {
            apply(sp.getContent()); // follow logical content, skip skin internals
            return;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                apply(child);
            }
        }
    }

    /**
     * Adds mouse-entered/exited handlers to {@code btn} that swap between
     * its current (normal) style and a computed hover style.
     * Safe to call multiple times — each call replaces any prior handlers.
     */
    public static void attachHover(Button btn) {
        String s = btn.getStyle();
        if (s == null || s.isBlank()) return;
        String hover = computeHoverStyle(s);
        if (hover == null || hover.equals(s)) return;
        final String normal  = s;
        final String hovered = hover;
        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited (e -> btn.setStyle(normal));
    }

    /**
     * Derives a hover-state CSS string from a button's normal inline style.
     * Returns {@code null} if no hover variation is defined for this pattern.
     */
    /**
     * Plays a short horizontal shake on {@code node} to draw attention to an error.
     * Total duration ~420 ms; amplitude dampens from ±8 px to 0.
     */
    public static void shake(Node node) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,         new KeyValue(node.translateXProperty(),  0)),
            new KeyFrame(Duration.millis(70),   new KeyValue(node.translateXProperty(), -8)),
            new KeyFrame(Duration.millis(140),  new KeyValue(node.translateXProperty(),  8)),
            new KeyFrame(Duration.millis(210),  new KeyValue(node.translateXProperty(), -6)),
            new KeyFrame(Duration.millis(280),  new KeyValue(node.translateXProperty(),  6)),
            new KeyFrame(Duration.millis(350),  new KeyValue(node.translateXProperty(), -3)),
            new KeyFrame(Duration.millis(420),  new KeyValue(node.translateXProperty(),  0))
        );
        tl.play();
    }

    public static String computeHoverStyle(String s) {
        // Danger / red background — one subtle step lighter
        if (s.contains("-fx-background-color: #d4183d"))
            return s.replace("#d4183d", "#dc2850");

        // Primary dark buttons (#0f172a — login / form screens) — one step to slate-800
        if (s.contains("-fx-background-color: #0f172a"))
            return s.replace("#0f172a", "#1e293b");

        // Primary dark buttons (#030213 — dashboard screens) — one step to near-navy
        if (s.contains("-fx-background-color: #030213"))
            return s.replace("#030213", "#0f172a");

        // Glass / translucent button (Start screen "Staff Member") — slightly more opaque
        if (s.contains("rgba(255,255,255,0.07)"))
            return s.replace("rgba(255,255,255,0.07)", "rgba(255,255,255,0.16)");

        // White outline buttons (Refresh, Export CSV, category filter…) — very subtle tint
        if (s.contains("-fx-background-color: white") && s.contains("-fx-border-color"))
            return s.replace("-fx-background-color: white", "-fx-background-color: #f1f5f9");

        // Light-gray button (Select All) — one step darker
        if (s.contains("-fx-background-color: #f3f4f6"))
            return s.replace("#f3f4f6", "#e5e7eb");

        // Transparent / ghost buttons
        if (s.contains("-fx-background-color: transparent")) {
            if (s.contains("#d4183d"))
                // Red-text ghost: Log out — soft red tint
                return s.replace("-fx-background-color: transparent",
                                 "-fx-background-color: rgba(212,24,61,0.08)");
            if (s.contains("#94a3b8"))
                // Gray-text ghost on dark screens: Back button on login / register
                return s.replace("-fx-background-color: transparent",
                                 "-fx-background-color: rgba(255,255,255,0.10)");
            // All other ghost buttons: Clear Filters, Cancel, Deselect All, icon buttons…
            return s.replace("-fx-background-color: transparent",
                             "-fx-background-color: rgba(0,0,0,0.06)");
        }

        return null;
    }
}
