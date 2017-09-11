package game;

import game.menu.*;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.ResourceManagerClass;
import kslib.audio.*;
import kslib.audio.stochastic_music.MarkovMidi;
import kslib.globals.MiscGlobals;
import kslib.graphics.KSFont;
import kslib.graphics.KSGraphicsConstants;
import kslib.graphics._3d.*;
import kslib.graphics._3d.model.vbo.ModelVBO;
import kslib.graphics._3d.procedural.Primatives;
import kslib.io.save_data.Achievements;
import kslib.io.save_data.Preferences;
import kslib.ui.*;
import kslib.ui.menu.*;
import kslib.ui.menu.menu_options.MenuOption;

import static kslib.graphics.KSGraphicsConstants.*;
import static kslib.globals.MiscGlobals.*;

import static game.AceMahjongConstants.*;
import static game.MahjongLayout.*;

public class ResourceManager extends ResourceManagerClass {
	public static Midi music;
	public static WaveBuffer swipe, win, lose, click;
	public static Model3d tile_model, achievement_tile_model;
	public static AbstractModel3d tile_models[] = new AbstractModel3d[36];
	public static KSTexture[] textures = new KSTexture[36];
	public static FullscreenBillboard menu_background;
	public static Model3d padlock_billboard;
	public static KSTexture padlock_texture;
	public static KSFont font_tiny, font_small, font_preview_layout, font_not_free, font_menu_option, font_title, font_subtitle, font_hs_entry;
	public static Model3d title_tiles[] = new Model3d[256];
	public static KSTexture background_texture;
	public static Model3d button, wideButton;
	public static Button pause, hint, shuffle;
	public static Button rotClockwise, rotAnticlockwise, rotUp, rotDown;
	public static Model3d cameraLabelImage;
	public static Model3d achievement_tiles[] = new Model3d[30];
	public static Button achievement_buttons[] = new Button[30];
	public static KSTexture[] achievement_textures = new KSTexture[30];
	public static MahjongLayout layouts[];
	public static MenuScreen menu_main, menu_main_with_savegames,
							menu_highscores,
							menu_highscore_entry,
							menu_achievements,
							menu_pause,
							menu_register,
							menu_editor,
							menu_options;
	public static MessageBox help;
	public static DialogBox nag_dialog;
	public static ModelVBO ground_plane;
	
	public static int selected_layout = 0;
	
	public static final String LAYOUTS_PATH = kslib.data_baking.DataBaking.convert_documents_folder_path("/Users/benwheatley/Documents/Business/Other resources/Mahjong Layouts/");
	
	public static final String ground_plane_model_path = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Ace Mahjong/board.ksmesh");
	public static final String ground_plane_texture_path = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Ace Mahjong/board.png");
	
	public void load() {
		// If logo is null, load logo and exit
		if (menu_loading_screen.logo==null) {
			menu_loading_screen.logo = new Model3d(new File(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Corporate imagery/Glyph_3d_bevel.obj"))	);
			menu_loading_screen.load_progress = 0;
			return;
		}
		// Load fonts
		if (font_small==null) {
			font_small = new KSFont("Arial Narrow", Font.PLAIN, 2.5f);
			menu_loading_screen.font = font_small;
			menu_loading_screen.load_progress++;
			return;
		}
		if (font_tiny==null) {	font_tiny = new KSFont("Arial Narrow", Font.PLAIN, 1.75f); menu_loading_screen.load_progress++; return;	}
		if (font_preview_layout==null) {	font_preview_layout = new KSFont("Arial Narrow", Font.PLAIN, 4); menu_loading_screen.load_progress++; return;	}
		if (font_title==null) {	font_title = new KSFont("Herculanum", Font.BOLD, 13); menu_loading_screen.load_progress++; return; }
		if (font_subtitle==null) {	font_subtitle = new KSFont("Herculanum", Font.ITALIC, 4); menu_loading_screen.load_progress++; return; }
		if (font_not_free==null) {	font_not_free = new KSFont("Herculanum", Font.BOLD, 4.0f); menu_loading_screen.load_progress++; return; }
		if (font_menu_option==null) {	font_menu_option = new KSFont("Herculanum", Font.BOLD, 5.5f); menu_loading_screen.load_progress++; return; }
		if (font_hs_entry==null) {	font_hs_entry = new KSFont("Arial Narrow", Font.PLAIN, 4.0f); menu_loading_screen.load_progress++; return; }
		if (help==null) {
			float y = (-GameShell.display_region_height)/2;
			help = new MessageBox(HELP_BASIC, font_small, 0, y);
			help.ypos = y + help.height; // Difference between ycent and ypos is important
			help.text_h_alignment = ALIGN_H_CENTRE;
			menu_loading_screen.load_progress++; return;
		}
		// Load the generic background texture I use everywhere...
		if (background_texture==null) {
			background_texture = new KSTexture(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Backgrounds/Blue Marble/Blue_Marble_texture.jpg")	);
			ground_plane = new ModelVBO(
					new File(ground_plane_model_path),
					new KSTexture(ground_plane_texture_path, GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true)		);
			ground_plane.customRenderer = OutlineRenderer.getInstance();
			menu_loading_screen.load_progress += 2; return;
		}
		// Load buttons
		if (button==null) {
			loadButtons();
			menu_loading_screen.load_progress += 3; return;
		}
		// Nag box
		if (nag_dialog==null) {
			buildNagDialog();
			menu_loading_screen.load_progress += 2; return;
		}
		if (padlock_billboard==null) {
			padlock_texture = new KSTexture(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Corporate imagery/Padlock.png")	);
			padlock_billboard = Primatives.rectangle(10, 10, padlock_texture);
			menu_loading_screen.load_progress++; return;
		}
		// Load other graphics
		if (tile_models[35]==null) {
			loadMainContent();
			menu_loading_screen.load_progress += 0.2;
			return;
		}
		if (achievement_pane==null) {
			loadAchievements();
			menu_loading_screen.load_progress += 0.2;
			return;
		}
		// Load layouts
		if (layouts==null) {
			loadLayouts(	new File(	LAYOUTS_PATH	)	);
		}
		if (getLayoutPreviews()) {
			menu_loading_screen.load_progress += 1;
			return;
		}
		// Load music
		if (music==null) {
			try {
				music = MarkovMidi.make_markov_midi(new File(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Audio resources/KS Markov files/oriental_4.kmf")));
			} catch (IOException e) {
				if (DEBUG) e.printStackTrace();
			}
			menu_loading_screen.load_progress++; return;
		}
		// Load sounds
		try {
			if (click==null) {
				click = new WaveBuffer(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Audio resources/click.wav"));
				menu_loading_screen.load_progress++; return;
			}
			if (swipe==null) {
				swipe = new WaveBuffer(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Audio resources/swipe_piece_up.wav"));
				menu_loading_screen.load_progress++; return;
			}
			if (win==null) {
				win = new WaveBuffer(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Audio resources/win.wav"));
				menu_loading_screen.load_progress++; return;
			}
			if (lose==null) {
				lose = new WaveBuffer(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Audio resources/lose.wav"));
				menu_loading_screen.load_progress++; return;
			}
		}
		catch (AudioException e) {
			if (DEBUG) e.printStackTrace();
		}

		// Make menu-related stuff
		if (menu_background==null) {
			menu_background = new FullscreenBillboard( background_texture );
			buildMenus();
			menu_loading_screen.load_progress++; return;
		}
		
		// Mark ourselves as having finished loading
		GameShell.loaded = true;
	}
	
	private void buildNagDialog() {
//		float NAG_OK_X = 2.5f, NAG_OK_Y = -5;
//		float NAG_BUY_X = -2.5f, NAG_BUY_Y = -5;
//		float NAG_TITLE_Y = 14;
//		float NAG_WIDTH = 50, NAG_HEIGHT = 40;
//		Button ok = new Button(	NAG_OK_X,	NAG_OK_Y,	button, "OK",			font_small, GL_COLOUR_4F_BLACK, OVERALL_SCALE);
//		Button buy = new Button(NAG_BUY_X,	NAG_BUY_Y,	button, "Buy it now!",	font_small, GL_COLOUR_4F_BLACK, OVERALL_SCALE);
//		UIComponent title = new UIComponent(-NAG_WIDTH/2, NAG_TITLE_Y, NAG_WIDTH, font_menu_option.get_default_line_spacing(), null, GameShell.game_name, font_menu_option, 1);
//		ok.xpos -= button.bounding_radius;
//		buy.xpos -= button.bounding_radius;
//		Model3d nag_model = Primatives.rectangle(NAG_WIDTH, NAG_HEIGHT, background_texture, 2);
//		nag_dialog = new DialogBox(-NAG_WIDTH/2, -NAG_HEIGHT/2, NAG_WIDTH, NAG_HEIGHT, nag_model, NAG_TEXT, font_small, 1, true, title, ok, buy);
//		nag_dialog.text_h_alignment = KSGraphicsConstants.ALIGN_H_LEFT;
//		float ct[] = KSGraphicsConstants.GL_COLOUR_4F_WHITE;
//		nag_dialog.setThisTextColours(ct,ct,ct);
//		nag_dialog.title.setThisTextColours(ct,ct,ct);
//		float c[] = {.2f, .2f, .2f, 1f};
//		nag_dialog.setThisModelColours(c, c, c);
//		for (Button b : nag_dialog.buttons) {
//			b.setThisModelColours(KSGraphicsConstants.GL_COLOUR_4F_WHITE,
//				KSGraphicsConstants.GL_COLOUR_4F_GREEN,
//				KSGraphicsConstants.GL_COLOUR_4F_BLACK);				
//			b.setThisTextColours(KSGraphicsConstants.GL_COLOUR_4F_BLACK,
//					KSGraphicsConstants.GL_COLOUR_4F_BLACK,
//					KSGraphicsConstants.GL_COLOUR_4F_BLACK);
//		}
		float NAG_BUTTON_Y = -4.25f*OVERALL_SCALE;
		float NAG_TITLE_Y = 14;
		float NAG_WIDTH = 50, NAG_HEIGHT = 40;
		Button later = new Button(	0,	NAG_BUTTON_Y,	button,		"Later",			font_small, GL_COLOUR_4F_BLACK, 2		);
		Button buy = new Button(	0,	NAG_BUTTON_Y,	wideButton,	"Buy full version",	font_small, GL_COLOUR_4F_BLACK, 2.2f	);
		later.setXposByCentre(	  1+( buy.width	 	* buy.scale)/2		);
		buy.setXposByCentre(	-(1+( later.width	* later.scale)/2)	);
		UIComponent title = new UIComponent(-NAG_WIDTH/2, NAG_TITLE_Y, NAG_WIDTH, font_menu_option.get_default_line_spacing(), null, "Ace Mahjong", font_menu_option, 1);
		later.xpos -= button.bounding_radius;
		buy.xpos -= button.bounding_radius;
		Model3d nag_model = Primatives.rectangle(NAG_WIDTH, NAG_HEIGHT, background_texture, 2);
		nag_dialog = new DialogBox(-NAG_WIDTH/2, -NAG_HEIGHT/2, NAG_WIDTH, NAG_HEIGHT, nag_model, null, null, 1, true, title, later, buy);
		UIComponent nagText = nag_dialog.padInner(title.height/2, later.height, 2, 2);
		nagText.font = font_small;
		nagText.relative_line_spacing *= 1.1f;
		nagText.setText(NAG_TEXT, true);
		nagText.text_h_alignment = KSGraphicsConstants.ALIGN_H_LEFT;
		nagText.text_v_alignment = KSGraphicsConstants.ALIGN_V_TOP;
		nag_dialog.addSubcomponents(nagText);
		float ct[] = KSGraphicsConstants.GL_COLOUR_4F_WHITE;
		nag_dialog.setAllTextColours(ct,ct,ct);
		nag_dialog.title.setAllTextColours(ct,ct,ct);
		float c[] = {.2f, .2f, .2f, 1f};
		nag_dialog.setThisModelColours(c, c, c);
		for (Button b : nag_dialog.buttons) {
			b.setThisModelColours(KSGraphicsConstants.GL_COLOUR_4F_WHITE,
					KSGraphicsConstants.GL_COLOUR_4F_GREEN,
					KSGraphicsConstants.GL_COLOUR_4F_BLACK);				
			b.setThisTextColours(KSGraphicsConstants.GL_COLOUR_4F_BLACK,
					KSGraphicsConstants.GL_COLOUR_4F_BLACK,
					KSGraphicsConstants.GL_COLOUR_4F_BLACK);
			b.clipToRect = false;
		}
	}
	
	private static String basicButtonModelPath = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/UI_components/button.obj");
	private static String wideButtonModelPath = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/UI_components/button_extra_wide.obj");
	private static String rotClockwiseTexturePath = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Misc/Arrows/rotateClockwise.png");
	private static String rotAnticlockwiseTexturePath = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Misc/Arrows/rotateAnticlockwise.png");
	private static String rotUpTexturePath  = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Misc/Arrows/up.png");
	private static String rotDownTexturePath = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Misc/Arrows/down.png");
	private static String cameraLabelTexturePath = kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Misc/Symbols/Camera/Camera.png");
	private void loadButtons() {
		
		button = new Model3d(new File(	basicButtonModelPath	));
		button.apply_texture(background_texture, Model3d.TEX_MODE_XY);
		
		wideButton = new Model3d(new File(	wideButtonModelPath	));
		wideButton.apply_texture(background_texture, Model3d.TEX_MODE_XY);
		
		float BUTTON_Y = SCALE_Y*((GRID_EXTENT_Y-3.66f)-OFFSET_Y);
		pause =	new PauseButton(	SCALE_X*((GRID_EXTENT_X-19)-OFFSET_X),	BUTTON_Y,	button, "Pause/Menu",	font_small, GL_COLOUR_4F_WHITE, OVERALL_SCALE);
		hint =	new Button(			SCALE_X*((GRID_EXTENT_X-12)-OFFSET_X), 	BUTTON_Y,	button, "Hint",			font_small, GL_COLOUR_4F_WHITE, OVERALL_SCALE);
		shuffle =	new Button(		SCALE_X*((GRID_EXTENT_X-5)-OFFSET_X), 	BUTTON_Y,	button, "Zap",			font_small, GL_COLOUR_4F_WHITE, OVERALL_SCALE);
		
		Model3d rotClockwiseModel = Primatives.rectangle(1, 1, new KSTexture(rotClockwiseTexturePath, GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true)),
				rotAnticlockwiseModel = Primatives.rectangle(1, 1, new KSTexture(rotAnticlockwiseTexturePath, GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true)),
				rotUpModel = Primatives.rectangle(1, 1, new KSTexture(rotUpTexturePath, GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true)),
				rotDownModel = Primatives.rectangle(1, 1, new KSTexture(rotDownTexturePath, GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true));
		cameraLabelImage = Primatives.rectangle(1, 1, new KSTexture(cameraLabelTexturePath, GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true));
		rotClockwise = new Button(	0, 0,	rotClockwiseModel, null, null, GL_COLOUR_4F_WHITE, OVERALL_SCALE);
		rotAnticlockwise = new Button(	0, 0,	rotAnticlockwiseModel, null, null, GL_COLOUR_4F_WHITE, OVERALL_SCALE);
		rotUp = new Button(	0, 0,	rotUpModel, null, null, GL_COLOUR_4F_WHITE, OVERALL_SCALE);
		rotDown = new Button(	0, 0,	rotDownModel, null, null, GL_COLOUR_4F_WHITE, OVERALL_SCALE);
		
		Button all_buttons[] = {pause, hint, shuffle, rotClockwise, rotAnticlockwise, rotUp, rotDown};
		for (Button b : all_buttons) {
			b.setThisModelColours(KSGraphicsConstants.GL_COLOUR_4F_WHITE,
				KSGraphicsConstants.GL_COLOUR_4F_GREEN,
				KSGraphicsConstants.GL_COLOUR_4F_BLACK);
			b.setThisTextColours(KSGraphicsConstants.GL_COLOUR_4F_WHITE,
					KSGraphicsConstants.GL_COLOUR_4F_WHITE,
					KSGraphicsConstants.GL_COLOUR_4F_BLACK);
			b.clipToRect = false;
		}
//		float width = OVERALL_SCALE*(GRID_EXTENT_X+1)*SCALE_X;
//		float height = OVERALL_SCALE*(GRID_EXTENT_Y+1)*SCALE_Y;
//		float corner_radius = OVERALL_SCALE*SCALE_X;
	}
	
	/** @return	is there more work to do? */
	private boolean getLayoutPreviews() {
		for (int i=0; i<layouts.length; ++i) {
			MahjongLayout l = layouts[i];
			if (l.preview==null) {
				l.prepare_preview();
				return true;
			}
		}
		return false;
	}
	
	private File layouts_folder_children[];
	private static FileFilter file_filter = new FileFilter() {
		public boolean accept(File f) {
			return !f.isDirectory() && f.getName().endsWith(MahjongLayout.FILENAME_EXTENSION);
		}
	};
	
	public boolean layouts_have_changed(File layouts_folder) {
		File new_children[] = layouts_folder.listFiles(file_filter);
		if (layouts_folder_children.length!=new_children.length) return true;
		for (int i=0; i<layouts_folder_children.length; ++i) {
			File ith = layouts_folder_children[i];
			boolean file_found = false;
			for (int j=0; j<new_children.length; ++j) {
				File jth = new_children[j];
				if (ith.compareTo(jth)==0) file_found = true;
			}
			if (!file_found) return true;
		}
		return false;
	}
	
	public void loadLayouts(File layouts_folder) {
		Integer layout_hash = Preferences.get(KEY_SELECTED_LAYOUT);
		String fallback_layout = "Dragon.aml";
		
		layouts_folder_children = layouts_folder.listFiles(file_filter);
		MahjongLayout local_layouts[] = new MahjongLayout[layouts_folder_children.length];
		int i=0;
		for (File child : layouts_folder_children) {
			if (child!=null) {
				try {
					local_layouts[i] = new MahjongLayout(child);
					if (layout_hash==null) {
						if (child.getName().compareToIgnoreCase(fallback_layout)==0) selected_layout = i;
					}
					else {
						if (local_layouts[i].hash == layout_hash.intValue()) {
							selected_layout = i;
						}
					}
					++i;
				} catch (IOException e) {
					// Assume it's just not the correct file type, and fail quietly
				}
			}
		}
		layouts = new MahjongLayout[i];
		System.arraycopy(local_layouts, 0, layouts, 0, layouts.length);
	}
	
	private void buildMenus() {
		menu_x = 5-(GameShell.display_region_width/2);
		menu_y = 16;
		menu_width = 60;
		menu_height = font_menu_option.get_default_line_spacing();
		menu_col_normal = KSGraphicsConstants.GL_COLOUR_4F_WHITE;
		menu_col_hover = KSGraphicsConstants.GL_COLOUR_4F_CYAN;
		menu_col_disabled = new float[]{0.5f,0.5f,0.5f,1};
		
		rebuildMenusWithBuyOption();
		
		float highscore_menu_y = -35;
		MenuOption[] actual_menu_options;
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_HIGHSCORES, MenuFactory.MENU_EXTRA_NONE, font_menu_option, menu_x, highscore_menu_y, menu_height, menu_width, menu_height);
		menu_highscores = new HighscoresMenuScreen(menu_background, actual_menu_options, ALIGN_H_LEFT, ALIGN_V_TOP);
		menu_highscores.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_highscores.set_all_click_sounds(click);
		
		Rectangle2D rect = font_small.get_string_box(STANDARD_NAME_STRING);
		float input_width = (float)rect.getWidth();
		float input_height = (float)font_small.getFontSize();
		
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_HIGHSCORE_ENTRY_SCREEN, MenuFactory.MENU_EXTRA_PLAY_AGAIN, font_menu_option, menu_x, highscore_menu_y, menu_height, menu_width, menu_height);
		menu_highscore_entry = new HighscoreEntryMenuScreen(	menu_background, actual_menu_options,
				input_width, input_height, font_small, GL_COLOUR_4F_BLACK, ALIGN_H_LEFT, ALIGN_V_TOP	);
		menu_highscore_entry.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_highscore_entry.set_all_click_sounds(click);
		
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_ACHIEVEMENTS, MenuFactory.MENU_EXTRA_NONE, font_menu_option, menu_x, highscore_menu_y, menu_height, menu_width, menu_height);
		menu_achievements = new AchievementsMenuScreen(	menu_background, actual_menu_options, ALIGN_H_LEFT, ALIGN_V_TOP	);
		menu_achievements.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_achievements.set_all_click_sounds(click);
		
		rect = font_hs_entry.get_string_box(STANDARD_NAME_STRING);
		input_width = (float)rect.getWidth();
		input_height = (float)font_hs_entry.getFontSize();
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_REGISTER, MenuFactory.MENU_EXTRA_NONE, font_menu_option, menu_x, highscore_menu_y+menu_height, menu_height, menu_width, menu_height);
		menu_register = new RegistrationMenuScreen(	menu_background, actual_menu_options, font_hs_entry, GL_COLOUR_4F_WHITE, input_width, input_height, ALIGN_H_LEFT, ALIGN_V_TOP);
		menu_register.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_register.set_all_click_sounds(click);
		
		float narrow_width = 15;
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_EDITOR, MenuFactory.MENU_EXTRA_NONE, font_menu_option, menu_x, highscore_menu_y+menu_height*2, menu_height, narrow_width, menu_height);
		menu_editor = new EditorMenuScreen(	menu_background, actual_menu_options, ALIGN_H_LEFT, ALIGN_V_TOP	);
		menu_editor.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_editor.set_all_click_sounds(click);
		
		MenuSystem.register_menus(	menu_main, menu_highscores, menu_achievements, menu_pause, menu_highscore_entry, menu_register, menu_editor, menu_options	);
		MenuSystem.set_all_music(music);
	}
	
	private static String texture_file_name[] = {
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-1.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-2.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-3.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-4.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-5.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-6.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-7.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-8.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Bamboo-9.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-1.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-2.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-3.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-4.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-5.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-6.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-7.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-8.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Circle-9.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-1.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-2.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-3.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-4.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-5.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-6.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-7.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-8.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Number-9.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Dragon-Green.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Dragon-Red.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Dragon-Blank.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Season-Autumn.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Flower-Bamboo.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Wind-North.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Wind-South.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Wind-West.jpg"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/MahjongTiles/Rasterised/JPEGs/Wind-East.jpg"),
	};
	private boolean loadMainContent() {
		if (tile_model==null) {
			tile_model = new Model3d(new File(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Tiles/Mahjong/generic_tile_lowpoly.obj"))	);
			return false;
		}
		for (int i=0; i<textures.length; ++i) {
			if (textures[i]==null) {
				textures[i] = new KSTexture(texture_file_name[i], GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true);
				if ((i&1)==0) return false;
			}
		}
		for (int i=0; i<36; ++i) {
			if (tile_models[i]==null) {
				Model3d tmp = new Model3d(tile_model);
				tmp.apply_texture(textures[i], Model3d.TEX_MODE_XY);
				tile_models[i] = tmp;// = new ModelVBO(tmp);
				tile_models[i].customRenderer = OutlineRenderer.getInstance();
				if ((i&1)==0) return false;
			}
		}
		return true;
	}
	
	private static final String achievementPaths[] = {
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/finish_a_game.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/finish_5_games.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/finish_50_games.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/finish_500_games.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/finish_5000_games.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/win_achievement.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/win_achievement_x10.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/win_achievement_x25.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/win_achievement_x100.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/win_achievement_x1000.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/win_achievement_x2000.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/time_achievement_bronze.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/time_achievement_silver.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/time_achievement_gold.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/wind_of_change.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/no_hints_achievement.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/no_cheats_achievement.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/reliable_victory.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/dependable_victory.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/constant_victory.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/unfailing_victory.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/10k_gc.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/25k_gc.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/50k_gc.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/100k_gc.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/Ace_Mahjong/skilled_cub.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/Ace_Mahjong/skilled_fox.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/Ace_Mahjong/skilled_kitsune.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/Ace_Mahjong/master_kitsune.png"),
		kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Achievements/Ace_Mahjong/veteran_kitsune.png"),
	};
	private boolean loadAchievements() {
		if (achievement_tile_model==null) {
			achievement_tile_model = new Model3d(new File(	kslib.data_baking.DataBaking.convert_resource_path("/Users/benwheatley/Documents/Business/Graphics resources/Tiles/Mahjong/generic_tile.obj"))	);
			return false;
		}
		for (int i=0; i<achievement_textures.length; ++i) {
			if (achievement_textures[i]==null) {
				achievement_textures[i] = new KSTexture(achievementPaths[i], GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true);
				return false;
			}	
		}
		for (int i=0; i<achievement_tiles.length; i++) {
			if (achievement_tiles[i]==null) {
				achievement_tiles[i] = new Model3d(achievement_tile_model);
				achievement_tiles[i].apply_texture(achievement_textures[i], Model3d.TEX_MODE_SHEET_XY, 0.75f);
				float XS = SCALE_X*2.1f*OVERALL_SCALE;
				float YS = SCALE_Y*2.1f*OVERALL_SCALE;
				achievement_buttons[i] = new Button(
						((i%15)-7)*XS, (4*OVERALL_SCALE)-(YS*(i/15)),
						achievement_tiles[i], null, null, OVERALL_SCALE);
				Achievements.achievements[i].model = achievement_tiles[i];
				return false;
			}
		}
		achievement_pane = new AchievementPane(achievement_buttons);
		GameShell.achievement_font = font_small;
		GameShell.achievement_scale = OVERALL_SCALE;
		return true;
	}
	
	public void rebuildMenusWithBuyOption() {
		menu_height = font_menu_option.get_default_line_spacing();
		MenuOption[] actual_menu_options;
		
		// Normal "main menu"
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_MAIN,
				MenuFactory.MENU_EXTRA_REGISTER |
				MenuFactory.MENU_EXTRA_CUSTOM_OPTIONS |
				MenuFactory.MENU_EXTRA_EDITOR |
				MenuFactory.MENU_EXTRA_FULLSCREEN |
				MenuFactory.MENU_EXTRA_MUSIC,
				font_menu_option, menu_x, menu_y, menu_height, menu_width, menu_height);
		menu_main = new MainMenuScreen(menu_background, MiscGlobals.MENU_MAIN, actual_menu_options, ALIGN_H_CENTRE, ALIGN_V_TOP);
		menu_main.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_main.set_all_click_sounds(click);
		
		// Alternate "main menu", this one WITH "Continue saved game"
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_MAIN,
				MenuFactory.MENU_EXTRA_REGISTER |
				MenuFactory.MENU_EXTRA_CUSTOM_OPTIONS |
				MenuFactory.MENU_EXTRA_EDITOR |
				MenuFactory.MENU_EXTRA_FULLSCREEN |
				MenuFactory.MENU_EXTRA_LOAD_SAVE_GAME |
				MenuFactory.MENU_EXTRA_MUSIC,
				font_menu_option, menu_x, menu_y, menu_height, menu_width, menu_height);
		menu_main_with_savegames = new MainMenuScreen(menu_background, MiscGlobals.MENU_MAIN, actual_menu_options, ALIGN_H_CENTRE, ALIGN_V_TOP);
		menu_main_with_savegames.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_main_with_savegames.set_all_click_sounds(click);
		menu_main_with_savegames.music = music;
		
		// Pause menu
		float menu_x_pause = -menu_width/2;
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_PAUSE,
				MenuFactory.MENU_EXTRA_REGISTER |
				MenuFactory.MENU_EXTRA_FULLSCREEN |
				MenuFactory.MENU_EXTRA_MUSIC,
				font_menu_option, menu_x_pause, menu_y, menu_height, menu_width, menu_height);
		menu_pause = new PauseMenuScreen(menu_background, MiscGlobals.MENU_PAUSE, actual_menu_options, ALIGN_H_CENTRE, ALIGN_V_TOP);
		menu_pause.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_pause.set_all_click_sounds(click);
		
		float options_width = 50;
		float options_menu_y = (menu_height*2.5f)-(GameShell.display_region_height/2);
		actual_menu_options = MenuFactory.make_menu(MiscGlobals.MENU_OPTIONS, MenuFactory.MENU_EXTRA_START_GAME | MenuFactory.MENU_EXTRA_REGISTER, font_menu_option, menu_x, options_menu_y, menu_height, options_width, menu_height);
		menu_options = new SelectLayoutMenuScreen(	menu_background, actual_menu_options, ALIGN_H_LEFT, ALIGN_V_TOP	);
		menu_options.set_all_colours(menu_col_normal, menu_col_hover, menu_col_disabled);
		menu_options.set_all_click_sounds(click);
		
		// Rebuild the relevant parts of the menu system
		MenuSystem.register_menus(	menu_main, menu_pause, menu_options	);
		MenuSystem.set_all_music(music);
	}
}
