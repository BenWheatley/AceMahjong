package game;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import kslib.GameShell;
import kslib.editors.*;
import kslib.graphics.KSGraphics2D;
import kslib.graphics.KSGraphicsConstants;
import kslib.graphics._3d.KSTexture;
import kslib.io.save_data.Achievements;
import kslib.io.save_data.Preferences;
import kslib.util.StringUtil;

import static com.sun.opengl.util.Screenshot.readToBufferedImage;
import static game.AceMahjongConstants.*;
import static game.ResourceManager.*;

public class MahjongLayout extends Level {
	
	public static final int LATEST_VERSION = 1;
	public static final int GRID_EXTENT_X = 16*2,
							GRID_EXTENT_Y = 10*2,
							GRID_EXTENT_Z = 9;
	public boolean cell_matrix[][][];
	
	private Tile preview_cells[][][];
	public KSTexture preview;
	
	public String name;
	public int hash;
	
	public int wins, plays;
	
	public File source_file, preview_file;
	
	public static final String FILENAME_EXTENSION = ".aml";
	public String get_filename_extension() {	return FILENAME_EXTENSION;	}
	
	public MahjongLayout() {	cell_matrix = new boolean[GRID_EXTENT_X][GRID_EXTENT_Y][GRID_EXTENT_Z];	}
	public MahjongLayout(File f) throws IOException {
		super(f);
		source_file = f;
		preview_file = new File(f.getParent(), "."+StringUtil.replaceExtension(f.getName(), ".png"));
		String KEY;
		KEY = get_games_played_key(name);
		Integer games_played_this_layout = Preferences.get(KEY);
		if (games_played_this_layout==null) games_played_this_layout = 0;
		plays = games_played_this_layout;
		KEY = get_games_won_key(name);
		Integer games_won_this_layout = Preferences.get(KEY);
		if (games_won_this_layout==null) games_won_this_layout = 0;
		wins = games_won_this_layout;
	}
	
	public void new_level() {
		cell_matrix = new boolean[GRID_EXTENT_X][GRID_EXTENT_Y][GRID_EXTENT_Z];
		has_unsaved_changes = false;
	}
	
	public void load(File f) throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		int version = dis.readByte();
		
		if (version>LATEST_VERSION) throw new IOException("Version too recent");
		
		int extent_x, extent_y, extent_z;
		if (version==0) {
			extent_x = 16*2;
			extent_y = 9*2;
			extent_z = 9;
		}
		else {
			extent_x = GRID_EXTENT_X;
			extent_y = GRID_EXTENT_Y;
			extent_z = GRID_EXTENT_Z;
		}
		cell_matrix = new boolean[GRID_EXTENT_X][GRID_EXTENT_Y][GRID_EXTENT_Z];
		for (int x=0; x<extent_x; ++x) {
			for (int y=0; y<extent_y; ++y) {
				for (int z=0; z<extent_z; ++z) {
					cell_matrix[x][y][z] = dis.readBoolean();
				}
			}
		}
		
		name = f.getName();
		name = name.substring(0, name.length()-4); // Remove file extension and period
		name = StringUtil.capitaliseEachWord(name);
		hash = StringUtil.calculate_32_bit_hash(name);
	}
	
	public void save(File f) throws IOException {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
		dos.writeByte(LATEST_VERSION); // version
		for (int x=0; x<GRID_EXTENT_X; ++x) {
			for (int y=0; y<GRID_EXTENT_Y; ++y) {
				for (int z=0; z<GRID_EXTENT_Z; ++z) {
					dos.writeBoolean(cell_matrix[x][y][z]);
				}
			}
		}
	}
	
	public File get_default_editor_folder() {
		return new File(	LAYOUTS_PATH	);
	}
	
	public void prepare_preview() {
		
		if (preview!=null) return; // Test for quick exit
		
		GL gl = GLU.getCurrentGL();
		
		int i;
		preview_cells = new Tile[GRID_EXTENT_X][GRID_EXTENT_Y][GRID_EXTENT_Z];
		final int MAX = 144;
		Tile temp[] = new Tile[MAX];
		for (i=0; i<MAX; ++i) temp[i] = new Tile(i/4);
		i=0;
		for (int x=0; x<GRID_EXTENT_X && i<MAX; ++x) {
			for (int y=0; y<GRID_EXTENT_Y && i<MAX; ++y) {
				for (int z=0; z<GRID_EXTENT_Z && i<MAX; ++z) {
					if (cell_matrix[x][y][z]) {
						preview_cells[x][y][z] = temp[i];
						++i;
					}
				}
			}
		}
		
		if (preview_file.exists()) {
			// Load from disk
			preview = new KSTexture(preview_file.getAbsolutePath(), GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true);
			preview.setWrap(GL.GL_CLAMP);
		}
		else {
			// Rasterize and save to disk
			int new_width = 512, new_height = 512;
			render_preview(gl, new_width, new_height);
		}
	}
	
	private void render_preview(GL gl, int new_width, int new_height) {
		gl.glPushAttrib(GL.GL_VIEWPORT_BIT | GL.GL_LIGHTING_BIT);
		
		gl.glViewport(0, 0, new_width, new_height);
		
		gl.glClearColor(0, 0, 0, 0.5f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		
		gl.glColor4f(1, 1, 1, 1);
		
		// Lit stuff
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glMultTransposeMatrixd(DISPLAY_MATRIX, 0); // OpenGL is transposed relative to normal matrix writing style
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		final float xoff = -GRID_EXTENT_X/2;
		final float yoff = -GRID_EXTENT_Y/2;
		
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glLoadIdentity();
		for (int z=0; z<GRID_EXTENT_Z; ++z) {
			for (int x=GRID_EXTENT_X-1; x>=0; --x) {
				for (int y=GRID_EXTENT_Y-1; y>=0; --y) {
					Tile this_cell = preview_cells[x][y][z];
					if (this_cell!=null) {
						gl.glPushMatrix();
						gl.glScalef(OVERALL_SCALE, OVERALL_SCALE, OVERALL_SCALE);
						gl.glTranslatef(
										(x+xoff)*SCALE_X,
										(y+yoff)*SCALE_Y,
										z*SCALE_Z
												);
						ResourceManager.tile_models[GameShell.rand.nextInt(ResourceManager.tile_models.length)].render(gl);
						gl.glPopMatrix();
					}
				}
			}
		}
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		BufferedImage image = readToBufferedImage(new_width, new_height, true);
		try {	ImageIO.write(image, "png", preview_file);	} catch (IOException e) {	/* Nothing we can do, user doesn't care, fail silently. */	}
		preview = new KSTexture(image, GL.GL_LINEAR, GL.GL_LINEAR_MIPMAP_LINEAR, true);
		preview.setWrap(GL.GL_CLAMP);
		
		gl.glPopAttrib();
	}
	
	private String get_games_played_key(String name_in) {	return KEY_GAMES_PLAYED_PREFIX+name_in;	}
	private String get_games_won_key(String name_in) {	return KEY_GAMES_WON_PREFIX+name_in;	}
	
	public void complete_a_game() {
		final String KEY = get_games_played_key(layouts[selected_layout].name);
		Integer games_played_this_layout = Preferences.get(KEY);
		if (games_played_this_layout==null) games_played_this_layout = 0;
		games_played_this_layout++;
		Preferences.set(KEY, games_played_this_layout);
		plays++;
	}
	
	public void win_a_game() {
		final String KEY = get_games_won_key(layouts[selected_layout].name);
		Integer games_won_this_layout = Preferences.get(KEY);
		if (games_won_this_layout==null) games_won_this_layout = 0;
		games_won_this_layout++;
		Preferences.set(KEY, games_won_this_layout);
		wins++;
		check_and_award_multi_layout_achievements();
	}
	
	private void check_and_award_multi_layout_achievements() {
		int more_than_5_wins = 0,
			more_than_7_wins = 0,
			more_than_10_wins = 0,
			more_than_15_wins = 0,
			more_than_20_wins = 0;
		for (int i=0; i<layouts.length; ++i) {
			MahjongLayout l = layouts[i];
			if (l!=null) {
				final String KEY = get_games_won_key(l.name);
				Integer wins = Preferences.get(KEY);
				if (wins==null) wins = 0;
				if (wins>=5) more_than_5_wins++; 
				if (wins>=7) more_than_7_wins++;
				if (wins>=10) more_than_10_wins++;
				if (wins>=15) more_than_15_wins++;
				if (wins>=20) more_than_20_wins++;
			}
		}
		if (more_than_5_wins>=5) Achievements.award_achivement(ACHIEVEMENT_SKILLED_CUB);
		if (more_than_7_wins>=7) Achievements.award_achivement(ACHIEVEMENT_SKILLED_FOX);
		if (more_than_10_wins>=10) Achievements.award_achivement(ACHIEVEMENT_SKILLED_KITSUNE);
		if (more_than_15_wins>=15) Achievements.award_achivement(ACHIEVEMENT_MASTER_KITSUNE);
		if (more_than_20_wins>=20) Achievements.award_achivement(ACHIEVEMENT_VETERAN_KITSUNE);
	}
	
}
