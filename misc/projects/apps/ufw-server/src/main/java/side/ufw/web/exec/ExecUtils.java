package side.ufw.web.exec;

import java.util.ArrayList;
import java.util.List;

public class ExecUtils {
    public static List<String> combineExecutableAndArgs(String executable, List<String> arguments) {
        var result = new ArrayList<String>(arguments.size() + 1);
        result.add(executable);
        result.addAll(arguments);
        return result;
    }

    public static List<String> concat(List<String> a, List<String> b) {
        var result = new ArrayList<String>(a.size() + b.size());
        result.addAll(a);
        result.addAll(b);
        return result;
    }
}
