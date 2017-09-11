package game.menu;

import static game.ResourceManager.*;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.graphics.KSFont;
import kslib.graphics.KSGraphicsConstants;
import kslib.ui.menu.BasicRegistrationMenuScreen;
import kslib.ui.menu.menu_options.MenuOption;
import kslib.util.Renderable;

public class RegistrationMenuScreen extends BasicRegistrationMenuScreen {
	
	public RegistrationMenuScreen(Renderable background_in, MenuOption[] options_in, KSFont input_font, float[] text_colour_in,
			float text_input_width, float text_input_height, int default_h_alignment, int default_v_alignment) {
		super(background_in, options_in, input_font, text_colour_in, text_input_width, text_input_height, default_h_alignment, default_v_alignment);
	}
	
	public void render(GL gl) {
		super.render(gl);
		
		float draw_y = 3*GameShell.display_region_height/8;
		font_title.draw_multiline("Ace Mahjong",
				KSGraphicsConstants.GL_COLOUR_4F_WHITE,
				0, draw_y,
				0, 0,
				KSGraphicsConstants.ALIGN_H_CENTRE, KSGraphicsConstants.ALIGN_V_MIDDLE,
				0,
				false);
	}

}
