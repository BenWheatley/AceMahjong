package game.menu;

import game.AceMahjongGame;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.graphics.KSFont;
import kslib.graphics.KSGraphicsConstants;
import kslib.ui.menu.*;
import kslib.ui.menu.menu_options.MenuOption;
import kslib.util.Renderable;
import kslib.util.StringUtil;

import static kslib.graphics.KSGraphicsConstants.*;

import static game.ResourceManager.*;
import static game.AceMahjongConstants.*;

public class HighscoreEntryMenuScreen extends GenericHighscoreEntryMenuScreen {
	
	static int countdown;
	
	static int displayScore;
	static int displayScoreBase;
	static int displayScoreBonusTime;
	static int displayScoreBonusHints;
	static int displayScoreBonusShuffles;
	
	public HighscoreEntryMenuScreen(Renderable background_in, MenuOption[] options_in,
			float text_input_width, float text_input_height, KSFont input_font, float[] text_colour_in, int default_h_alignment, int default_v_alignment) {
		super(background_in, options_in, input_font, text_colour_in,
				text_input_width, text_input_height, default_h_alignment, default_v_alignment);
	}
	
	public static void set_display_scores() {
		countdown = GameShell.TARGET_FPS;
		displayScore = 0;
		final AceMahjongGame the_game = (AceMahjongGame)(GameShell.game);
		displayScoreBase = the_game.score;
		if (last_highscore.winner) {
			displayScoreBonusTime = the_game.score_bonus_time;
			displayScoreBonusHints = the_game.score_bonus_hints;
			displayScoreBonusShuffles = the_game.score_bonus_shuffles;
		}
		else displayScoreBonusHints = displayScoreBonusShuffles = 0;
		the_game.score = displayScoreBase + displayScoreBonusHints + displayScoreBonusShuffles;
	}
	
	public void update() {
		super.update();
		if (displayScoreBase>0) {
			int delta = displayScoreBase/100;
			if (delta<23) delta = 23;
			if (delta>displayScoreBase) delta = displayScoreBase;
			displayScoreBase -= delta;
			displayScore += delta;
		}
		else if (displayScoreBonusTime>0) {
			if (countdown==0) {
				int delta = displayScoreBonusTime/30;
				if (delta<3) delta = 3;
				if (delta>displayScoreBonusTime) delta = displayScoreBonusTime;
				displayScoreBonusTime -= delta;
				displayScore += delta;
				if (displayScoreBonusTime==0) countdown=GameShell.TARGET_FPS;
			}
			else --countdown;
		}
		else if (displayScoreBonusHints>0) {
			if (countdown==0) {
				int delta = displayScoreBonusHints/30;
				if (delta<3) delta = 3;
				if (delta>displayScoreBonusHints) delta = displayScoreBonusHints;
				displayScoreBonusHints -= delta;
				displayScore += delta;
				if (displayScoreBonusHints==0) countdown=GameShell.TARGET_FPS;
			}
			else --countdown;
		}
		else if (displayScoreBonusShuffles>0) {
			if (countdown==0) {
				int delta = displayScoreBonusShuffles/30;
				if (delta<3) delta = 3;
				if (delta>displayScoreBonusShuffles) delta = displayScoreBonusShuffles;
				displayScoreBonusShuffles -= delta;
				displayScore += delta;
			}
			else --countdown;
		}
	}
	
	public void render(GL gl) {
		gl.glDisable(GL.GL_LIGHTING);
		super.render(gl);
		
		gl.glDisable(GL.GL_TEXTURE_2D);
		float col[] = KSGraphicsConstants.GL_COLOUR_4F_WHITE;
		float draw_y = 3*GameShell.display_region_height/8;
		font_title.draw_multiline("New Highscore", col, 0, draw_y, 0, 0, ALIGN_H_CENTRE, ALIGN_V_MIDDLE, 0, false);
		
		draw_y -= (font_menu_option.getFontSize()*2); 
		String result_detail = last_highscore.winner ? WIN_CAPTION : LOSE_CAPTION;
		font_menu_option.draw_multiline(result_detail, col, 0, draw_y, 0, 0, ALIGN_H_CENTRE, ALIGN_V_MIDDLE, font_menu_option.get_default_line_spacing(), false);
		
		float draw_x = 0;
		draw_y = 0;
		final KSFont hsFont = font_hs_entry;
		hsFont.draw_multiline("Please enter your name:  ", col, draw_x, draw_y, 0, 0, ALIGN_H_RIGHT, ALIGN_V_BASE, 0, false);
		draw_y -= input.font.getFontSize();
		hsFont.draw_multiline("Score:  ", col, draw_x, draw_y, 0, 0, ALIGN_H_RIGHT, ALIGN_V_MIDDLE, 0, false);
		hsFont.draw_multiline(StringUtil.FormatNumberLocally(displayScore), col, draw_x, draw_y, 0, 0, ALIGN_H_LEFT, ALIGN_V_MIDDLE, 0, false);
		
		if (last_highscore.winner) {
			draw_y -= input.font.getFontSize()*1.5;
			float opacity = 1;
			if (displayScoreBonusTime!=0) opacity = ((float)GameShell.TARGET_FPS-countdown)/GameShell.TARGET_FPS;
			hsFont.draw_multiline("Time bonus:  ", col[0], col[1], col[2], opacity, draw_x, draw_y, 0, 0, ALIGN_H_RIGHT, ALIGN_V_MIDDLE, 0, false);
			hsFont.draw_multiline(StringUtil.FormatNumberLocally(displayScoreBonusTime), col[0], col[1], col[2], opacity, draw_x, draw_y, 0, 0, ALIGN_H_LEFT, ALIGN_V_MIDDLE, 0, false);
			if (displayScoreBonusTime==0) {
				if (displayScoreBonusHints==0) opacity = 1;
				else opacity = ((float)GameShell.TARGET_FPS-countdown)/GameShell.TARGET_FPS;
				draw_y -= input.font.getFontSize();
				hsFont.draw_multiline("Hint bonus:  ", col[0], col[1], col[2], opacity, draw_x, draw_y, 0, 0, ALIGN_H_RIGHT, ALIGN_V_MIDDLE, 0, false);
				hsFont.draw_multiline(StringUtil.FormatNumberLocally(displayScoreBonusHints), col[0], col[1], col[2], opacity, draw_x, draw_y, 0, 0, ALIGN_H_LEFT, ALIGN_V_MIDDLE, 0, false);
				if (displayScoreBonusHints==0) {
					opacity = ((float)GameShell.TARGET_FPS-countdown)/GameShell.TARGET_FPS;
					draw_y -= input.font.getFontSize();
					hsFont.draw_multiline("Shuffle bonus:  ", col[0], col[1], col[2], opacity, draw_x, draw_y, 0, 0, ALIGN_H_RIGHT, ALIGN_V_MIDDLE, 0, false);
					hsFont.draw_multiline(StringUtil.FormatNumberLocally(displayScoreBonusShuffles), col[0], col[1], col[2], opacity, draw_x, draw_y, 0, 0, ALIGN_H_LEFT, ALIGN_V_MIDDLE, 0, false);
				}
			}
		}
		
	}
}
