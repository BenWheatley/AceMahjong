package game.menu;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.graphics.KSGraphicsConstants;
import kslib.io.save_data.Preferences;
import kslib.ui.menu.*;
import kslib.ui.menu.menu_options.MenuOption;
import kslib.util.Renderable;
import game.AceMahjongConstants;
import game.MahjongLayout;
import static kslib.graphics.KSGraphicsConstants.*;
import static game.ResourceManager.*;

public class AchievementsMenuScreen extends BasicAchievementsMenuScreen {
	public AchievementsMenuScreen(Renderable background_in, MenuOption[] options_in, int default_h_alignment, int default_v_alignment) {
		super(game.ResourceManager.font_small, background_in, options_in, default_h_alignment, default_v_alignment);
		text_colour_normal = KSGraphicsConstants.GL_COLOUR_4F_WHITE;
	}
	
	public void render(GL gl) {
		gl.glDisable(GL.GL_STENCIL_TEST);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		super.render(gl);
		
		float draw_y = 3*GameShell.display_region_height/8;
		font_title.draw_multiline("Achievements", text_colour_normal, 0, draw_y, 0, 0, ALIGN_H_CENTRE, ALIGN_V_MIDDLE, 0, false);
		
		if (achievement_pane.selected_achievement==-1) {
			Integer played = Preferences.get(AceMahjongConstants.KEY_GAMES_PLAYED);
			Integer won = Preferences.get(AceMahjongConstants.KEY_GAMES_WON);
			if (played==null) played = 0;
			if (won==null) won = 0;
			int percentage = 0;
			if (played!=0) percentage = (won*100)/played;
			Integer streak = Preferences.get(AceMahjongConstants.KEY_GAMES_WON_IN_A_ROW);
			if (streak==null) streak = 0;
			boolean streak_is_wins = streak>0;
			if (streak<0) streak=-streak;
			String summary = "Games played: "+played+"\n"+
							"Games won: "+won+
							(played==0? "" : " ("+percentage+"%)" +"\n"+
							"Last streak: "+streak+" "+(streak==1?(streak_is_wins?"win":"loss"):(streak_is_wins?"wins":"losses"))
										);
			float y = -2*font.get_default_line_spacing();
			font.draw_multiline(summary, text_colour_normal,
					-20, y, 0, 0,
					ALIGN_H_CENTRE, ALIGN_V_TOP,
					font.get_default_line_spacing(), false);
			
			float x1 = 15,
					x2 = 20,
					x3 = 27	;
			final float DELTA = font.get_default_line_spacing();
			font.draw_multiline("Layout", text_colour_normal,
					x1, y, 0, 0,	ALIGN_H_RIGHT, ALIGN_V_TOP,	0, false);
			font.draw_multiline("Plays", text_colour_normal,
					x2, y, 0, 0,	ALIGN_H_CENTRE, ALIGN_V_TOP,	0, false);
			font.draw_multiline("Wins", text_colour_normal,
					x3, y, 0, 0,	ALIGN_H_CENTRE, ALIGN_V_TOP,	0, false);
			y += DELTA* ( (menu_tick/(GameShell.TARGET_FPS/1.0)) % layouts.length );
			float c[] = new float[4];
			c[0] = text_colour_normal[0]; c[1] = text_colour_normal[1];
			c[2] = text_colour_normal[2];
			for (int i=0; i<layouts.length*2; ++i) {
				y -= DELTA;
				MahjongLayout l = layouts[i%layouts.length];
				float opacity;
				opacity = -y-(DELTA*2.5f);
				if (y<-DELTA*10) opacity = 1-(-y-(DELTA*10));
				c[3] = (float)opacity;
				font.draw_multiline(l.name, c,
						x1, y, 0, 0,	ALIGN_H_RIGHT, ALIGN_V_TOP,	0, false);
				font.draw_multiline(""+l.plays, c,
						x2, y, 0, 0,	ALIGN_H_CENTRE, ALIGN_V_TOP,	0, false);
				font.draw_multiline(""+l.wins, c,
						x3, y, 0, 0,	ALIGN_H_CENTRE, ALIGN_V_TOP,	0, false);
			}
		}
	}
}
