import android.os.Build;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class EmulatorDetector {

    public static boolean isEmulator(Context context) {
        return checkBuildProperties()
                || checkEmulatorFiles()
                || checkTelephonyManager(context)
                || checkPipes()
                || checkQEmuDriverFile()
                || checkQEmuProps();
    }

    private static boolean checkBuildProperties() {
        String[] knownEmulatorBuildProperties = {
                "ro.hardware", "goldfish",
                "ro.hardware", "ranchu",
                "ro.kernel.qemu", "1",
                "ro.product.model", "sdk",
                "ro.product.model", "google_sdk",
                "ro.product.model", "sdk_x86",
                "ro.product.model", "vbox86p"
        };

        for (int i = 0; i < knownEmulatorBuildProperties.length; i += 2) {
            String property = knownEmulatorBuildProperties[i];
            String value = knownEmulatorBuildProperties[i + 1];
            if (value.equals(getSystemProperty(property))) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkEmulatorFiles() {
        String[] knownEmulatorFiles = {
                "/dev/socket/qemud",
                "/dev/qemu_pipe",
                "/system/lib/libc_malloc_debug_qemu.so",
                "/sys/qemu_trace",
                "/system/bin/qemu-props"
        };

        for (String path : knownEmulatorFiles) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkTelephonyManager(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperatorName = tm.getNetworkOperatorName();
        return "Android".equalsIgnoreCase(networkOperatorName);
    }

    private static boolean checkPipes() {
        String[] knownEmulatorPipes = {
                "/dev/socket/qemud",
                "/dev/qemu_pipe"
        };

        for (String path : knownEmulatorPipes) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkQEmuDriverFile() {
        File driverFile = new File("/proc/tty/driver");
        if (driverFile.exists() && driverFile.canRead()) {
            byte[] data = new byte[(int) driverFile.length()];
            try {
                String driverData = new BufferedReader(new InputStreamReader(driverFile)).readLine();
                return driverData.contains("goldfish") || driverData.contains("qemu");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean checkQEmuProps() {
        int minQemuProps = 6;
        String[] knownQemuProps = {
                "ro.product.device", "qemu",
                "ro.product.brand", "generic",
                "ro.product.manufacturer", "unknown",
                "ro.product.model", "sdk",
                "ro.hardware", "goldfish",
                "ro.hardware", "ranchu"
        };

        int matchCount = 0;
        for (int i = 0; i < knownQemuProps.length; i+= 2) {
            String property = knownQemuProps[i];
            String value = knownQemuProps[i + 1];
            if (value.equals(getSystemProperty(property))) {
                matchCount++;
                }
            }
            return matchCount >= minQemuProps;
            }

private static String getSystemProperty(String propertyName) {
    String propertyValue = "";
    try {
        Class<?> systemPropertyClazz = Class.forName("android.os.SystemProperties");
        propertyValue = (String) systemPropertyClazz.getMethod("get", String.class).invoke(systemPropertyClazz, propertyName);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return propertyValue;
}
}