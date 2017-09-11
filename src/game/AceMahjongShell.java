package game;

import game.menu.OptionsMenuOption;

import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;

import kslib.GameShell;
import kslib.audio.*;
import kslib.globals.ISO_4217_Currency_Codes;
import kslib.globals.MiscGlobals;
import kslib.graphics.KSFont;
import kslib.graphics.KSGraphicsConstants;
import kslib.io.save_data.Achievements;
import kslib.io.save_data.Achievements.Achievement;
import kslib.registration.RegistrationData;
import kslib.registration.RegistrationModule;
import kslib.report_system.ReportSystem;
import kslib.ui.SelfFadingMessage;
import kslib.ui.menu.MenuSystem;
import kslib.ui.menu.menu_options.MenuOption;
import static kslib.globals.MiscGlobals.*;

import static game.ResourceManager.*;
import static game.AceMahjongConstants.*;

public class AceMahjongShell extends GameShell {
	// Interesting stuff first, housekeeping stuff last
	
	public static void main(String[]args) {
		try {
			capabilities = new GLCapabilities();
//			capabilities.setSampleBuffers(true);
//	        capabilities.setNumSamples(2);
	        capabilities.setStencilBits(8);
	        implementation = new AceMahjongShell(
					"Ace Mahjong Solitaire",
					"6FHED_Mahjong",
					new RegistrationModule.DefaultRegistrationModule()
							);
			GameShell.startup(implementation, new ResourceManager(), new CheatModule());
		}
		catch (ExceptionInInitializerError e) {
			display_nice_exception_in_initializer_error();
		}
	}
	
	public AceMahjongShell(String game_name_in, String kagi_product_db_name_in, RegistrationModule registration_module_in) {
		super(	game_name_in,
				kagi_product_db_name_in, 
				new String[][]{	{ISO_4217_Currency_Codes.CURR_CODE_US_DOLLAR, "15.99"}	},
				registration_module_in
					);
		achievement_align_x = KSGraphicsConstants.ALIGN_H_RIGHT;
	}
	
	public void update() {
		if (!loaded) return;
		
		if (MenuSystem.get_current_menu_index()==MENU_MAIN) {
			MenuSystem.register_menu(get_saved_game_file().exists() ?
						ResourceManager.menu_main_with_savegames:
						ResourceManager.menu_main );
		}
		if (!MenuSystem.update() && game!=null) {
			SoundSystem.playMusic(music);
			game.logic();
		}
	}
	
	// Housekeeping stuff
	
	public void gl_init(GLAutoDrawable gLDrawable) {
		gl_drawable_context = gLDrawable;
		GL gl = gLDrawable.getGL();
		
    	gl.glEnable(GL.GL_COLOR_MATERIAL);
    	float xl=41.5f,
			yl=30,
			zl=33.6f;
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, new float[]{xl,yl,zl,0}, 0);
    	float bright = 0.2f;
    	gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, new float[]{bright,bright,bright,1}, 0);
    	bright = 0.9f;
    	gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, new float[]{bright,bright,bright,1}, 0);
    	bright = 0.9f;
    	gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, new float[]{bright,bright,bright,1}, 0);
    	bright = 0.0f;
    	gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, new float[]{bright,bright,bright,1}, 0);
    	bright = 0.35f;
    	gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, new float[]{bright,bright,bright,1}, 0);
		
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearStencil(0);
		gl.glClearDepth(10000.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_FASTEST);
		gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_FASTEST);
		gl.glHint(GL.GL_POINT_SMOOTH_HINT, GL.GL_FASTEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public Achievement[] get_achievement_configuration() {
		return new Achievement[]{
//				new Achievements.Achievement(id, display_name, description, date_achieved),
				new Achievements.Achievement(ACHIEVEMENT_PURPOSEFUL_KITSUNE, "Purposeful Kitsune", "Finish a game", null),
				new Achievements.Achievement(ACHIEVEMENT_DETERMINED_KITSUNE, "Determined Kitsune", "Finish five games", null),
				new Achievements.Achievement(ACHIEVEMENT_DOGGED_KITSUNE, "Dogged Kitsune", "Finish fifty games", null),
				new Achievements.Achievement(ACHIEVEMENT_PERSISTENT_KITSUNE, "Persistent Kitsune", "Finish five hundred games", null),
				new Achievements.Achievement(ACHIEVEMENT_TIRELESS_KITSUNE, "Tireless Kitsune", "Finish five thousand games", null),
				new Achievements.Achievement(ACHIEVEMENT_CLEVER_KITSUNE, "Clever Kitsune", "Win a game", null),
				new Achievements.Achievement(ACHIEVEMENT_CUNNING_KITSUNE, "Cunning Kitsune", "Win ten games", null),
				new Achievements.Achievement(ACHIEVEMENT_CRAFTY_KITSUNE, "Crafty Kitsune", "Win twenty five games", null),
				new Achievements.Achievement(ACHIEVEMENT_CANNY_KITSUNE, "Canny Kitsune", "Win one hundred games", null),
				new Achievements.Achievement(ACHIEVEMENT_CALCULATING_KITSUNE, "Calculating Kitsune", "Win one thousand games", null),
				new Achievements.Achievement(ACHIEVEMENT_INDEFATIGABLE_KITSUNE, "Indefatigable Kitsune", "Win two thousand games", null),
				new Achievements.Achievement(ACHIEVEMENT_FAST_FOX, "Fast Fox", "Win a game in less than fifteen minutes", null),
				new Achievements.Achievement(ACHIEVEMENT_QUICK_BROWN_FOX, "Quick Brown Fox", "Win a game in less than ten minutes", null),
				new Achievements.Achievement(ACHIEVEMENT_FASTEST_FOX_IN_THE_WEST, "Fastest Fox in the West", "Win a game in less than five minutes", null),
				new Achievements.Achievement(ACHIEVEMENT_WIND_OF_CHANGE, "Wind of Change", "Win a game that started with less than ten matching pairs", null),
				new Achievements.Achievement(ACHIEVEMENT_OVERSIGHT, "Oversight", "Win a game without using any Hints", null),
				new Achievements.Achievement(ACHIEVEMENT_PLAYING_FAIR, "Playing Fair", "Win a game without using any Shuffles", null),
				new Achievements.Achievement(ACHIEVEMENT_RELIABLE_VICTORY, "Reliable Victory", "Win three games in a row", null),
				new Achievements.Achievement(ACHIEVEMENT_DEPENDABLE_VICTORY, "Dependable Victory", "Win seven games in a row", null),
				new Achievements.Achievement(ACHIEVEMENT_CONSTANT_VICTORY, "Constant Victory", "Win eleven games in a row", null),
				new Achievements.Achievement(ACHIEVEMENT_UNFAILING_VICTORY, "Unfailing Victory", "Win seventeen games in a row", null),
				new Achievements.Achievement(ACHIEVEMENT_DECENT_SCORE, "Decent Score", "Score over 10,000 points", null),
				new Achievements.Achievement(ACHIEVEMENT_GREAT_SCORE, "Great Score", "Score over 25,000 points", null),
				new Achievements.Achievement(ACHIEVEMENT_AWESOME_SCORE, "Awesome Score", "Score over 50,000 points", null),
				new Achievements.Achievement(ACHIEVEMENT_ULTIMATE_SCORE, "Ultimate Score", "Score over 100,000 points", null),
				new Achievements.Achievement(ACHIEVEMENT_SKILLED_CUB, "Skilled Cub", "Win five games on each of five different layouts", null),
				new Achievements.Achievement(ACHIEVEMENT_SKILLED_FOX, "Skilled Fox", "Win seven games on each of seven different layouts", null),
				new Achievements.Achievement(ACHIEVEMENT_SKILLED_KITSUNE, "Skilled Kitsune", "Win ten games on each of ten different layouts", null),
				new Achievements.Achievement(ACHIEVEMENT_MASTER_KITSUNE, "Master Kitsune", "Win fifteen games on each of fifteen different layouts", null),
				new Achievements.Achievement(ACHIEVEMENT_VETERAN_KITSUNE, "Veteran Kitsune", "Win twenty games on each of twenty different layouts", null),
		};
	}
	
	public void try_to_start_new_game_menu_handler() {
		game = new AceMahjongGame(ResourceManager.layouts[ResourceManager.selected_layout]);
		MenuSystem.target_menu = MENU_NONE_INGAME;
	}
	
	public void try_to_continue_saved_game() {
		try {
			if (RegistrationData.has_registered()) {				
				game = new AceMahjongGame();
			}
			else {
				messages.add(new SelfFadingMessage(0, 0, MSG_SAVED_GAMES_IN_FULL_VERSION_ONLY, font_not_free,
						KSGraphicsConstants.GL_COLOUR_4F_WHITE, MSG_BG_COL));
				MenuSystem.target_menu = MiscGlobals.MENU_MAIN;
			}
		}
		catch (IOException e) {
			// In the event of an error reading the saved game, this resets the main menu to the default main menu.
			get_saved_game_file().delete();
			MenuSystem.register_menu(ResourceManager.menu_main);
			MenuSystem.target_menu = MiscGlobals.MENU_MAIN;
			ReportSystem.report_error("Error when trying to continue a saved game! Deleting old saved game!");
		}
	}
	
	public float[] get_achievement_text_colour(Achievement a) {
		return KSGraphicsConstants.GL_COLOUR_4F_WHITE;
	}
	
	public MenuOption get_custom_options_menu_item(KSFont font_in, float menu_x_in, float menu_y_in, float menu_width_in, float menu_height_in) {
		return new OptionsMenuOption(font_in, menu_x_in, menu_y_in, menu_width_in, menu_height_in);
	}
}