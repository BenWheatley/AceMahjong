package game.menu;

import static game.ResourceManager.*;

import game.ResourceManager;

import java.text.DateFormat;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.globals.MiscGlobals;
import kslib.graphics.KSGraphicsConstants;
import kslib.io.save_data.Highscores;
import kslib.io.save_data.Highscores.HighscoreEntry;
import kslib.ui.menu.*;
import kslib.ui.menu.menu_options.MenuOption;
import kslib.util.Renderable;
import kslib.util.StringUtil;

import static kslib.graphics.KSGraphicsConstants.*;

public class HighscoresMenuScreen extends BasicMenuScreen {

	private static final float[] NORMAL_ENTRY_COLOUR = new float[]{0,0.75f,1,1};

	public HighscoresMenuScreen(Renderable background_in, MenuOption[] options_in, int default_h_alignment, int default_v_alignment) {
		super(background_in, MiscGlobals.MENU_HIGHSCORES, options_in, default_h_alignment, default_v_alignment);
	}
	
	public void render(GL gl) {
		super.render(gl);
		
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glDisable(GL.GL_LIGHTING);
		
		gl.glLoadIdentity();
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glBegin(GL.GL_QUADS);
		gl.glColor4d(0, 0, 0, 0.5);
		float x=43.87543213367462f, y=18.677937118239882f; // Derived from experiments with mouse coordinates
		gl.glVertex2d(x, y);
		gl.glVertex2d(-x, y);
		gl.glVertex2d(-x, -y);
		gl.glVertex2d(x, -y);
		gl.glEnd();
		
		float col[] = KSGraphicsConstants.GL_COLOUR_4F_WHITE;
		float draw_y = 3*GameShell.display_region_height/8;
		font_title.draw_multiline("Highscores", col, 0, draw_y, 0, 0, ALIGN_H_CENTRE, ALIGN_V_MIDDLE, 0, false);
		
		float Y = 15;
		float X0 = -40.5f,
				X1 = -40,
				X2 = -15,
				X3 = 0,
				X4 = 8,
				X5 = 20;
		font_small.draw("Name", col, X1, Y); font_small.draw("Level", col, X2, Y); font_small.draw("Score", col, X3, Y); font_small.draw("Date", col, X5, Y);
		HighscoreEntry top10[] = Highscores.get_best_overall(10);
		int loopmax = 10;
		if (top10.length<loopmax) loopmax = top10.length;
		if (top10.length==0) {
			font_hs_entry.draw_multiline(
					"You do not yet have a highscore.\n"+
					"Play a game to get a highscore!",
					KSGraphicsConstants.GL_COLOUR_4F_WHITE,
					0, 0, 0, 0,
					KSGraphicsConstants.ALIGN_H_CENTRE,
					KSGraphicsConstants.ALIGN_V_MIDDLE,
					font_hs_entry.get_default_line_spacing(), false);
		}
		else {
			col = NORMAL_ENTRY_COLOUR;
			float win_col[] = KSGraphicsConstants.GL_COLOUR_4F_CYAN;
			for (int i=0; i<loopmax; ++i) {
				Y = 16-(i+2)*3;
				HighscoreEntry h = top10[i];
				float this_col[] = h.is_equal_to(GenericHighscoreEntryMenuScreen.last_highscore) ? win_col : col;
				font_small.draw_right(i+1+")", this_col, X0, Y);
				font_small.draw(font_small.truncate(h.name, (X2-X1)-2), this_col, X1, Y);
				font_small.draw(font_small.truncate(get_level_set_name(h.level_set), (X3-X2)-2), this_col, X2, Y);
				font_small.draw(StringUtil.FormatNumberLocally(h.score), this_col, X3, Y);
				if (h.winner) font_small.draw("(Won)", this_col, X4, Y);
				DateFormat s = DateFormat.getDateTimeInstance();
				font_small.draw(s.format(h.date_stamp), this_col, X5, Y);
			}
		}
	}
	
	private String get_level_set_name(int level_set_hash) {
		for (int i=0; i<ResourceManager.layouts.length; ++i) {
			if (ResourceManager.layouts[i].hash == level_set_hash) return ResourceManager.layouts[i].name;
		}
		return "Unknown level";
	}
}
