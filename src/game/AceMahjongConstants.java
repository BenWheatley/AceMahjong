package game;

import static game.MahjongLayout.GRID_EXTENT_Z;

import java.awt.Color;

import kslib.GameShell;

public class AceMahjongConstants {
	public static final int DIFFICULTY_MODIFIER_EASY = 1,
							DIFFICULTY_MODIFIER_MEDIUM = 3,
							DIFFICULTY_MODIFIER_HARD = 9;
	public static final int BONUS_TIME_LIMIT_EASY = 20*60*GameShell.TARGET_UPS,
							BONUS_TIME_LIMIT_MEDIUM = 14*60*GameShell.TARGET_UPS,
							BONUS_TIME_LIMIT_HARD = 8*60*GameShell.TARGET_UPS;
	public static final int NORTH = 1,
							EAST = 2,
							SOUTH = 3,
							WEST = 4;
	
	public static final float OFFSET_Y = 7.25f;
	public static final float OFFSET_X = 13.5f;
	public static final float OFFSET_Z = 1;
	public static final float SCALE_Y = 2/2;
	public static final float SCALE_X = 1.341f/2;
	public static final float SCALE_Z = 0.503f;
	public static final float OVERALL_SCALE = 3.1f;
	
	public static double DISPLAY_MATRIX[];
	public static final double ZS = 0.5;
	static {
		DISPLAY_MATRIX = new double[]{
				1,	0,	ZS,	0,
				0,	1,	ZS,	0,
				0,	0,	1,	0,
				0,	0,	0,	1
		};
	}
	
	public static Color get_level_colour(int z, float s, float b) {
		return Color.getHSBColor(z/(float)GRID_EXTENT_Z, s, b);
	}
	
	// Preference/Save data keys
	public static final String KEY_GAMES_STARTED = "KEY_GAMES_STARTED";
	public static final String KEY_GAMES_PLAYED = "GAMES_PLAYED";
	public static final String KEY_GAMES_WON = "GAMES_WON";
	public static final String KEY_GAMES_WON_IN_A_ROW = "GAMES_WON_IN_A_ROW";
	public static final String KEY_SELECTED_LAYOUT = "SELECTED_LAYOUT";
	public static final String KEY_GAMES_PLAYED_PREFIX = "GAMES_PLAYED_";
	public static final String KEY_GAMES_WON_PREFIX = "GAMES_WON_";
	
	public static final String WIN_CAPTION = "Victory!";
	public static final String LOSE_CAPTION = "Defeated!\n(No more moves)";
	
	public static final String HELP_BASIC =
		"Connect matching tiles. Both tiles must be clear on the left and right.";

	public static final String MSG_SAVED_GAMES_IN_FULL_VERSION_ONLY =
			"Saved games can only be\n" +
			"continued in the full version.\n" +
			"\n" +
			"Buy full version to continue your game!";
	public static final String FREE_PLAYERS_ONLY_GET_DRAGON =
			"Layouts other than \"Dragon\" are\n" +
			"only available in the full version.\n" +
			"\n" +
			"Buy full version to play $1!";
	public static final float MSG_BG_COL[] = {0, 0, 0, 0.75f};
	
	public static final String NAG_TEXT =
		"\n" +
		"This is the FREE version of Ace Mahjong Solitaire! " +
		"If you like it (and I hope you do), please consider upgrading to the full version. You will get:\n\n" +
		"* 35 more layouts\n" +
		"* No more nag-screens like this one\n" +
		"* All for just $15.99\n";
	
	// Achievements
	/** Finish a game */
	public static final int ACHIEVEMENT_PURPOSEFUL_KITSUNE = 0;
	/** Finish five games */
	public static final int ACHIEVEMENT_DETERMINED_KITSUNE = 1;
	/** Finish fifty games */
	public static final int ACHIEVEMENT_DOGGED_KITSUNE = 2;
	/** Finish five hundred games */
	public static final int ACHIEVEMENT_PERSISTENT_KITSUNE = 3;
	/** Finish five thousand games */
	public static final int ACHIEVEMENT_TIRELESS_KITSUNE = 4;
	/** Win a game */
	public static final int ACHIEVEMENT_CLEVER_KITSUNE = 5;
	/** Win ten games */
	public static final int ACHIEVEMENT_CUNNING_KITSUNE = 6;
	/** Win twenty five games */
	public static final int ACHIEVEMENT_CRAFTY_KITSUNE = 7;
	/** Win one hundred games */
	public static final int ACHIEVEMENT_CANNY_KITSUNE = 8;
	/** Win one thousand games */
	public static final int ACHIEVEMENT_CALCULATING_KITSUNE = 9;
	/** Win two thousand games */
	public static final int ACHIEVEMENT_INDEFATIGABLE_KITSUNE = 10;
	/** Win a game in less than fifteen minutes */
	public static final int ACHIEVEMENT_FAST_FOX = 11;
	/** Win a game in less than ten minutes */
	public static final int ACHIEVEMENT_QUICK_BROWN_FOX = 12;
	/** Win a game in less than five minutes */
	public static final int ACHIEVEMENT_FASTEST_FOX_IN_THE_WEST = 13;
	/** Win a game that started with less than ten matching pairs */
	public static final int ACHIEVEMENT_WIND_OF_CHANGE = 14;
	/** Win a game without using any Hints */
	public static final int ACHIEVEMENT_OVERSIGHT = 15;
	/** Win a game without using any Cheats */
	public static final int ACHIEVEMENT_PLAYING_FAIR = 16;
	/** Win three games in a row */
	public static final int ACHIEVEMENT_RELIABLE_VICTORY = 17;
	/** Win seven games in a row */
	public static final int ACHIEVEMENT_DEPENDABLE_VICTORY = 18;
	/** Win eleven games in a row */
	public static final int ACHIEVEMENT_CONSTANT_VICTORY = 19;
	/** Win seventeen games in a row */
	public static final int ACHIEVEMENT_UNFAILING_VICTORY = 20;
	/** Score over 10,000 points */
	public static final int ACHIEVEMENT_DECENT_SCORE = 21;
	/** Score over 25,000 points */
	public static final int ACHIEVEMENT_GREAT_SCORE = 22;
	/** Score over 50,000 points */
	public static final int ACHIEVEMENT_AWESOME_SCORE = 23;
	/** Score over 100,000 points */
	public static final int ACHIEVEMENT_ULTIMATE_SCORE = 24;
	/** Win five games on each of five different layouts */
	public static final int ACHIEVEMENT_SKILLED_CUB = 25;
	/** Win seven games on each of seven different layouts */
	public static final int ACHIEVEMENT_SKILLED_FOX = 26;
	/** Win ten games on each of ten different layouts */
	public static final int ACHIEVEMENT_SKILLED_KITSUNE = 27;
	/** Win fifteen games on each of fifteen different layouts */
	public static final int ACHIEVEMENT_MASTER_KITSUNE = 28;
	/** Win twenty games on each of twenty different layouts */
	public static final int ACHIEVEMENT_VETERAN_KITSUNE = 29;
}
