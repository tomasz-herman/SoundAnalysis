package pl.edu.pw.mini.hermant;

import com.formdev.flatlaf.FlatIntelliJLaf;
import org.apache.commons.lang3.SystemUtils;
import pl.edu.pw.mini.hermant.gui.FrequencyAnalyzerForm;
import pl.edu.pw.mini.hermant.gui.TimeAnalyzerForm;
import pl.edu.pw.mini.hermant.gui.WindowBuilder;

public class Main {
    public static void main(String[] args) {
        if (!SystemUtils.IS_OS_LINUX) throw new UnsupportedOperationException("Unsupported OS");
        FlatIntelliJLaf.install();
        WindowBuilder builder = new WindowBuilder();
//        TimeAnalyzerForm form = new TimeAnalyzerForm();
        FrequencyAnalyzerForm form = new FrequencyAnalyzerForm();
        builder.setContentPane(form.getMainPanel()).
                setMenuBar(form.getMenuBar()).
                setSize(1920, 1080).
                setPreferredSize(1920, 1080).
                setMaximumSize(1920, 1080).
                buildFrame();
    }
}
