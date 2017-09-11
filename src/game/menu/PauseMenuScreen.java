package game.menu;

import static game.ResourceManager.*;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.graphics.KSGraphicsConstants;
import kslib.ui.menu.BasicMenuScreen;
import kslib.ui.menu.menu_options.MenuOption;
import kslib.util.Renderable;

public class PauseMenuScreen extends BasicMenuScreen {
	
	public PauseMenuScreen(Renderable background_in, int identity_in, MenuOption[] options_in, int default_h_alignment, int default_v_alignment) {
		super(background_in, identity_in, options_in, default_h_alignment, default_v_alignment);
	}
	
	public void render(GL gl) {
		super.render(gl);
		
		float draw_y = 3*GameShell.display_region_height/8;
		font_title.draw_multiline("ACE MAHJONG",
				KSGraphicsConstants.GL_COLOUR_4F_WHITE,
				0, draw_y,
				0, 0,
				KSGraphicsConstants.ALIGN_H_CENTRE, KSGraphicsConstants.ALIGN_V_MIDDLE,
				0,
				false);	
	}
}
