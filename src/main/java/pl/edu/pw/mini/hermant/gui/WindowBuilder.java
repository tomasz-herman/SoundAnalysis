package pl.edu.pw.mini.hermant.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.Dialog.ModalityType;

import static java.awt.Dialog.ModalityType.*;
import static java.awt.Frame.MAXIMIZED_BOTH;
import static java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT;
import static java.awt.Window.Type.*;
import static javax.swing.WindowConstants.*;

public final class WindowBuilder {

    private static final GraphicsDevice DEVICE =
            GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getDefaultScreenDevice();

    private static final boolean TRANSLUCENCY_SUPPORT =
            DEVICE.isWindowTranslucencySupported(TRANSLUCENT);

    private Dimension minimumSize = null;
    private Dimension preferredSize = null;
    private Dimension maximumSize = null;
    private Dimension size = null;
    private String title = "Window";
    private JMenuBar menuBar = null;
    private Container contentPane = null;
    private Component relativeTo = null;
    private Window.Type type = Window.Type.NORMAL;
    private int closeOperation = EXIT_ON_CLOSE;
    private boolean maximized = false;
    private Color color = (new JPanel()).getBackground();
    private Shape shape = (new JFrame()).getShape();
    private boolean alwaysOnTop = false;
    private boolean enabled = true;
    private boolean focusable = true;
    private boolean autoFocusRequest = false;
    private boolean visible = true;
    private boolean undecorated = false;
    private boolean resizable = true;
    private boolean fullscreen = false;
    private float opacity = 1.0f;
    private ModalityType modality = DOCUMENT_MODAL;
    private Frame owner = null;

    public WindowBuilder setMinimumSize(int width, int height) {
        minimumSize = new Dimension(width, height);
        return this;
    }

    public WindowBuilder setPreferredSize(int width, int height) {
        preferredSize = new Dimension(width, height);
        return this;
    }

    public WindowBuilder setMaximumSize(int width, int height) {
        maximumSize = new Dimension(width, height);
        return this;
    }

    public WindowBuilder setSize(int width, int height) {
        size = new Dimension(width, height);
        return this;
    }

    public WindowBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public WindowBuilder setMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
        return this;
    }

    public WindowBuilder setContentPane(Container contentPane) {
        this.contentPane = contentPane;
        return this;
    }

    public WindowBuilder setRelativeTo(Component relativeTo) {
        this.relativeTo = relativeTo;
        return this;
    }

    public WindowBuilder setExitOnClose() {
        this.closeOperation = EXIT_ON_CLOSE;
        return this;
    }

    public WindowBuilder setDisposeOnClose() {
        this.closeOperation = DISPOSE_ON_CLOSE;
        return this;
    }

    public WindowBuilder setHideOnClose() {
        this.closeOperation = HIDE_ON_CLOSE;
        return this;
    }

    public WindowBuilder setNothingOnClose() {
        this.closeOperation = DO_NOTHING_ON_CLOSE;
        return this;
    }

    public WindowBuilder setNormalType() {
        this.type = NORMAL;
        return this;
    }

    public WindowBuilder setUtilityType() {
        this.type = UTILITY;
        return this;
    }

    public WindowBuilder setPopupType() {
        this.type = POPUP;
        return this;
    }

    public WindowBuilder setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        return this;
    }

    public WindowBuilder setMaximized(boolean maximized) {
        this.maximized = maximized;
        return this;
    }

    public WindowBuilder setColor(Color color) {
        this.color = color;
        return this;
    }

    public WindowBuilder setShape(Shape shape) {
        this.shape = shape;
        return this;
    }

    public WindowBuilder setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
        return this;
    }

    public WindowBuilder setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public WindowBuilder setFocusable(boolean focusable) {
        this.focusable = focusable;
        return this;
    }

    public WindowBuilder setAutoFocusRequest(boolean autoFocusRequest) {
        this.autoFocusRequest = autoFocusRequest;
        return this;
    }

    public WindowBuilder setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public WindowBuilder setUndecorated(boolean undecorated) {
        this.undecorated = undecorated;
        return this;
    }

    public WindowBuilder setOpacity(float opacity) {
        this.opacity = opacity;
        return this;
    }

    public WindowBuilder setResizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    public WindowBuilder setDocumentModal() {
        this.modality = DOCUMENT_MODAL;
        return this;
    }

    public WindowBuilder setApplicationModal() {
        this.modality = APPLICATION_MODAL;
        return this;
    }

    public WindowBuilder setToolkitModal() {
        this.modality = TOOLKIT_MODAL;
        return this;
    }

    public WindowBuilder setModeless() {
        this.modality = MODELESS;
        return this;
    }

    public WindowBuilder setOwner(Frame owner) {
        this.owner = owner;
        return this;
    }

    @NotNull
    public JFrame buildFrame() {
        if (contentPane == null) {
            System.err.println("Content pane wasn't specified.");
            contentPane = new JPanel();
        }
        if (opacity != 1.0 && !TRANSLUCENCY_SUPPORT) {
            System.err.println("Translucency is not supported ");
            opacity = 1.0f;
        }
        JFrame window = new JFrame();
        window.setType(type);
        window.setOpacity(opacity);
        window.setContentPane(contentPane);
        window.setJMenuBar(menuBar);
        window.setDefaultCloseOperation(closeOperation);
        window.setUndecorated(undecorated);
        window.setAlwaysOnTop(alwaysOnTop);
        window.setFocusable(focusable);
        window.setAutoRequestFocus(autoFocusRequest);
        window.setShape(shape);
        window.getContentPane().setBackground(color);
        window.setResizable(resizable);
        window.setEnabled(enabled);
        window.setTitle(title);
        if (minimumSize != null) window.setMinimumSize(minimumSize);
        if (preferredSize != null) window.setPreferredSize(preferredSize);
        if (maximumSize != null) window.setMaximumSize(maximumSize);
        window.pack();
        if (size != null) window.setSize(size);
        window.setLocationRelativeTo(relativeTo);
        window.setExtendedState(maximized ? MAXIMIZED_BOTH : Frame.NORMAL);
        if (fullscreen) DEVICE.setFullScreenWindow(window);
        window.setVisible(visible);
        return window;
    }

    @NotNull
    public JDialog buildDialog() {
        if (contentPane == null) {
            System.err.println("Content pane wasn't specified.");
            contentPane = new JPanel();
        }
        if (opacity != 1.0 && !TRANSLUCENCY_SUPPORT) {
            System.err.println("Translucency is not supported ");
            opacity = 1.0f;
        }
        if (closeOperation == EXIT_ON_CLOSE) closeOperation = DISPOSE_ON_CLOSE;
        JDialog dialog = owner == null ? new JDialog() : new JDialog(owner, modality);
        dialog.setType(type);
        dialog.setOpacity(opacity);
        dialog.setContentPane(contentPane);
        dialog.setJMenuBar(menuBar);
        dialog.setDefaultCloseOperation(closeOperation);
        dialog.setUndecorated(undecorated);
        dialog.setAlwaysOnTop(alwaysOnTop);
        dialog.setFocusable(focusable);
        dialog.setAutoRequestFocus(autoFocusRequest);
        dialog.setShape(shape);
        dialog.getContentPane().setBackground(color);
        dialog.setResizable(resizable);
        dialog.setEnabled(enabled);
        dialog.setTitle(title);
        if (minimumSize != null) dialog.setMinimumSize(minimumSize);
        if (preferredSize != null) dialog.setPreferredSize(preferredSize);
        if (maximumSize != null) dialog.setMaximumSize(maximumSize);
        dialog.pack();
        if (size != null) dialog.setSize(size);
        dialog.setLocationRelativeTo(relativeTo);
        dialog.setVisible(visible);
        return dialog;
    }
}