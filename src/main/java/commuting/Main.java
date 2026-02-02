package commuting;

import commuting.ui.LoginUI;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class Main {
    public static void main(String[] args){
        FlatMacLightLaf.setup();
        new LoginUI();
    }
}

