
package scs.azacord.uinterface;

import java.util.Vector;

import scs.azacord.service.Systemcall;
import scs.azacord.service.Cache;
import scs.azacord.uinterface.Input;

public class Display {

    private static Object mutex = new Object();

    private static Vector<String> screenBuffer = new Vector<String>();

    private static void trimCheck () { synchronized (mutex) {

        if (screenBuffer.size() > 50) for (int i = 0; i < 10; ++i)
                screenBuffer.remove(0);
    } }

    public static void append (String sender, String message) { synchronized (mutex) {

        screenBuffer.add("[" + sender + "]: " + message); trimCheck();
    } }

    public static void append (String message) { synchronized (mutex) {

        screenBuffer.add(message); trimCheck();
    } }

    public static void clear () { synchronized (mutex) {

        screenBuffer.clear();
    } }

    public static void tick () {

        runChecks();
        timer++; if (timer > 100) {
            runOccasionalChecks(); timer = 0; }

    }
    private static int timer;

    private static String inputCache;
    private static int screenBufferCache, widthCache, heightCache;

    private static void runChecks () {

        if (screenBufferCache != screenBuffer.size()) {
            screenBufferCache = screenBuffer.size(); render();
        }

        if (inputCache != Input.getValue()) {
            inputCache = Input.getValue(); inputOnlyRender();
        }
    }

    private static void runOccasionalChecks () {

        int widthResult = Systemcall.getConsoleWidth();
        if (widthCache != widthResult) {
            widthCache = widthResult; render();
        }

        int heightResult = Systemcall.getConsoleHeight();
        if (heightCache != heightResult) {
            heightCache = heightResult; render();
        }
    }

    private static void render () { synchronized (mutex) {

        int width = Systemcall.getConsoleWidth();
        int height = Systemcall.getConsoleHeight();

        Systemcall.canonicalEnable();
        System.out.print("\033[2J\033[1;1H");
        System.out.flush();

        for (String line : screenBuffer)
            System.out.println(line);

        if (screenBuffer.size() < height)
            for (int i = 2; i < height - screenBuffer.size(); ++i)
                System.out.println();

        String channel = Cache.getCurrentChannelName();
        for (int i = 0; i < 4; ++i) System.out.print("─");
        System.out.print(channel);
        for (int i = 0; i < width - channel.length() - 4; ++i)
            System.out.print("─");

        inputFieldCache = ">: " + (
            inputCache.length() > width - 16
            ? inputCache.substring(inputCache.length() - (width - 16))
            : inputCache
        );
        System.out.print(inputFieldCache);

        Systemcall.canonicalDisable();
    } }

    private static String inputFieldCache = "";

    private static void inputOnlyRender () { synchronized (mutex) {

        int width = Systemcall.getConsoleWidth();
        Systemcall.canonicalEnable();

        for (int i = 0; i < inputFieldCache.length() + 2; ++i)
            System.out.print("    \b\b\b\b\b");

        inputFieldCache = ">: " + (
            inputCache.length() > width - 16
            ? inputCache.substring(inputCache.length() - (width - 16))
            : inputCache
        );
        System.out.print(inputFieldCache);

        Systemcall.canonicalDisable();
    } }
}