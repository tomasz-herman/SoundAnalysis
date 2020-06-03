package pl.edu.pw.mini.hermant;

import com.formdev.flatlaf.FlatIntelliJLaf;
import org.apache.commons.lang3.SystemUtils;
import pl.edu.pw.mini.hermant.gui.AnalyzerChooserForm;
import pl.edu.pw.mini.hermant.gui.Form;
import pl.edu.pw.mini.hermant.gui.WindowBuilder;

public class Main {
    public static void main(String[] args) {
        if (!SystemUtils.IS_OS_LINUX) throw new UnsupportedOperationException("Unsupported OS");
        FlatIntelliJLaf.install();
        WindowBuilder builder = new WindowBuilder();
        Form form = new AnalyzerChooserForm();
        builder.setContentPane(form.getMainPanel()).
                setSize(320, 120).
                setTitle("Choose analysis type:").
                setResizable(false).
                buildFrame();
    }
}
