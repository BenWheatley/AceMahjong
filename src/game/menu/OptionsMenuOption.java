package game.menu;

import static kslib.globals.MiscGlobals.MENU_OPTIONS;
import kslib.graphics.KSFont;
import kslib.ui.menu.menu_options.SimpleTransitionMenuOption;

public class OptionsMenuOption extends SimpleTransitionMenuOption {
	
	public OptionsMenuOption(KSFont font_in, float menu_x, float menu_y, float menu_width, float menu_height) {
		super(MENU_OPTIONS, "Select layout...", font_in, menu_x, menu_y, menu_width, menu_height);
	}
}
