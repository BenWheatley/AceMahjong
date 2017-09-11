package game.menu;

import static game.ResourceManager.*;
import game.MahjongLayout;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.globals.MiscGlobals;
import kslib.graphics.KSFont;
import kslib.graphics.KSGraphicsConstants;
import kslib.graphics._3d.Model3d;
import kslib.graphics._3d.procedural.Primatives;
import kslib.ui.Button;
import kslib.ui.menu.BasicMenuScreen;
import kslib.ui.menu.MenuSystem;
import kslib.ui.menu.menu_options.MenuOption;
import kslib.util.Renderable;

public class MainMenuScreen extends BasicMenuScreen {
	
	Model3d selected_level_preview;
	Button selected_level_preview_button;
	
	public MainMenuScreen(Renderable background_in, int identity_in, MenuOption[] options_in, int default_h_alignment, int default_v_alignment) {
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
		draw_y -= font_title.get_default_line_spacing()/2;
		font_subtitle.draw_multiline("Solitaire",
				KSGraphicsConstants.GL_COLOUR_4F_WHITE,
				GameShell.display_region_width/4, draw_y,
				0, 0,
				KSGraphicsConstants.ALIGN_H_CENTRE, KSGraphicsConstants.ALIGN_V_MIDDLE,
				0,
				false);
		
		final MahjongLayout preview_layout = layouts[selected_layout];
		float width = GameShell.display_region_width*0.35f;
		float height = width;
		float SCALE = 1;
		float	x = GameShell.display_region_width*0.25f,
				y = 0;
		if (selected_level_preview_button==null) {
			selected_level_preview = Primatives.rounded_rectangle(width, height, width/10);
			if (selected_level_preview==null) {
				preview_layout.prepare_preview();
			}
			selected_level_preview.apply_texture(preview_layout.preview, Model3d.TEX_MODE_SHEET_XY, 0.75f);
			selected_level_preview_button = new Button(x-width/2, y-height/2, selected_level_preview, "", null, SCALE);
			float model_normal_in[] = KSGraphicsConstants.GL_COLOUR_4F_WHITE,
				model_hover_in[] = KSGraphicsConstants.GL_COLOUR_4F_CYAN,
				model_disabled_in[] = KSGraphicsConstants.GL_COLOUR_4F_BLACK;
			selected_level_preview_button.setThisModelColours(model_normal_in, model_hover_in, model_disabled_in);
			selected_level_preview_button.mouse_lmb_click_sound = click;
		}
		selected_level_preview_button.render(gl);
		gl.glTranslated(x, y, 0);
		gl.glScaled(SCALE, SCALE, SCALE);
		gl.glEnable(GL.GL_BLEND);
		KSFont f = font_preview_layout;
		float y2 = selected_level_preview_button.ypos+selected_level_preview_button.height-f.get_default_line_spacing()/2;
		f.draw_multiline("Selected layout:", KSGraphicsConstants.GL_COLOUR_4F_WHITE,
				selected_level_preview_button.xpos, y2, selected_level_preview_button.width, 0,
				KSGraphicsConstants.ALIGN_H_CENTRE, KSGraphicsConstants.ALIGN_V_MIDDLE,
				0, false);
		float y3 = selected_level_preview_button.ypos+f.get_default_line_spacing()/2;
		f.draw_multiline(preview_layout.name, KSGraphicsConstants.GL_COLOUR_4F_WHITE,
				selected_level_preview_button.xpos, y3, selected_level_preview_button.width, 0,
				KSGraphicsConstants.ALIGN_H_CENTRE, KSGraphicsConstants.ALIGN_V_MIDDLE,
				0, false);
	}
	
	public void update() {
		super.update();
		if (menu_tick<=0) selected_level_preview_button = null;
		if (selected_level_preview_button!=null) {
			selected_level_preview_button.update();
			if (selected_level_preview_button.something_happened && selected_level_preview_button.mouse_lmb_click) {
				MenuSystem.target_menu = MiscGlobals.MENU_OPTIONS;
			}
		}
	}
}
