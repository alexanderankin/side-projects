package asdf.model;

import picocli.CommandLine;

public class AsdfVersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        String implementationVersion = getClass().getPackage().getImplementationVersion();
        if (implementationVersion == null) {
            return new String[0];
        }
        return new String[]{implementationVersion};
    }
}
