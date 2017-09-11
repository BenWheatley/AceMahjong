package game;

import static game.ResourceManager.*;

import game.menu.HighscoreEntryMenuScreen;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Date;

import javax.media.opengl.GL;
import javax.vecmath.Vector3f;

import kslib.*;
import kslib.audio.SoundSystem;
import kslib.globals.MiscGlobals;
import kslib.graphics.FullScreenEffects;
import kslib.graphics.KSGraphicsConstants;
import kslib.graphics._3d.KSGLCamera;
import kslib.graphics._3d.util.KSGLUtil;
import kslib.io.KeyboardBuffer;
import kslib.io.MouseBuffer;
import kslib.io.save_data.*;
import kslib.io.save_data.Highscores.HighscoreEntry;
import kslib.math.KSMath;
import kslib.registration.RegistrationData;
import kslib.ui.*;
import kslib.ui.menu.*;
import kslib.util.*;

import static kslib.globals.MiscGlobals.*;
import static kslib.util.StringUtil.*;

import static game.AceMahjongConstants.*;
import static game.MahjongLayout.*;

public class AceMahjongGame extends KSGame {
	
	private static final float HUD_HEIGHT = 13.5f;
	private static final String TILES_DO_NOT_MATCH = "Tiles do not match";
	private static final String TILE_IS_BLOCKED = "Tile is blocked";
	private static final String CLICK_THE_FLASHING_TILES = "Click the flashing tiles";
	private static final String NO_MORE_HINTS_THIS_GAME = "No more hints this game";
	private static final String NO_MORE_SHUFFLES_THIS_GAME = "No more shuffles this game";
	private static final String NO_MOVES_REMAINING_TRY_ZAPPING_OR_CHEATING = "No moves remaining,\nuse a shuffle to continue.";
	
	Tile cells[][][];
	int remaining_hints = 5, remaining_shuffles = 10;
	
	public int score_bonus_time, score_bonus_hints = 0, score_bonus_shuffles = 0;
	
	Tile selected_cell;
	private boolean tile_has_been_clicked_on;
	private boolean no_hints_used = true, no_shuffles_used = true;
	private int initial_matching_pair_count;
	
	private boolean nag = false;
	
	private boolean shadow_map_dirty = true;
	
	private KSGLCamera camera;
	
	/* Reloads from save game, if it can */
	public AceMahjongGame() throws IOException {
		init();
		File save_game = GameShell.implementation.get_saved_game_file();
		DataInputStream dis = new DataInputStream(new FileInputStream(save_game));
		int version = dis.readByte();
		game_tick = dis.readInt();
		remaining_hints = dis.readByte();
		remaining_shuffles = dis.readByte();
		for (int x=0; x<GRID_EXTENT_X; ++x) {
			for (int y=0; y<GRID_EXTENT_Y; ++y) {
				for (int z=0; z<GRID_EXTENT_Z; ++z) {
					int value = dis.readByte();
					if (value!=-1) cells[x][y][z] = new Tile(value);
				}
			}
		}
		no_hints_used = dis.readBoolean();
		no_shuffles_used = dis.readBoolean();
		initial_matching_pair_count = dis.readByte();
		updateRemainingMoveCount();
	}
	
	private void init() {
		cells = new Tile[GRID_EXTENT_X][GRID_EXTENT_Y][GRID_EXTENT_Z];
		positionCamera();
	}
	
	private float cameraDistance = 105;
	private float cameraPitch = 1.0f;
	private float cameraYaw = KSMath.PI;
	private void positionCamera() {
		float r1 = (float)Math.cos(cameraPitch);
		float	eyeX = r1*(float)Math.sin(cameraYaw), eyeY = r1*(float)Math.cos(cameraYaw), eyeZ = (float)Math.sin(cameraPitch),
				centerX = 0, centerY = -1.5f, centerZ = 0,
				upX = 0, upY = 0, upZ = 1;
		Vector3f eye = new Vector3f(eyeX, eyeY, eyeZ);
		eye.scale(cameraDistance);
		camera = new KSGLCamera(	eye,
				new Vector3f(centerX, centerY, centerZ),
				new Vector3f(upX, upY, upZ)					);
	}
	
	public void save_and_exit() {
		try {
			File save_game = GameShell.implementation.get_saved_game_file();
			if (count_remaining_tiles()==0) {
				delete_save_game();
			}
			else {
				DataOutputStream dos = new DataOutputStream(new FileOutputStream(save_game));
				dos.writeByte(0); // version
				dos.writeInt(game_tick);
				dos.writeByte(remaining_hints);
				dos.writeByte(remaining_shuffles);
				for (int x=0; x<GRID_EXTENT_X; ++x) {
					for (int y=0; y<GRID_EXTENT_Y; ++y) {
						for (int z=0; z<GRID_EXTENT_Z; ++z) {
							if (!cell_exists(x, y, z)) dos.writeByte(-1);
							else dos.writeByte(cells[x][y][z].get_id());
						}
					}
				}
				dos.writeBoolean(no_hints_used);
				dos.writeBoolean(no_shuffles_used);
				dos.writeByte(initial_matching_pair_count);
			}
		}
		catch (Exception e) { /* Nothing we *can* do! */}
		MenuSystem.target_menu = MENU_MAIN;
	}
	
	private void delete_save_game() {
		File save_game = GameShell.implementation.get_saved_game_file();
		if (save_game.exists()) {
			save_game.delete();
		}
	}
	
	public AceMahjongGame(MahjongLayout layout) {
		int MIN_MATCHING_PAIRS=3;
		init();
		int i;
		final int MAX = 144;
		Tile temp[] = new Tile[MAX];
		for (i=0; i<MAX; ++i) temp[i] = new Tile(i/4);
		do {
			ArrayUtil.shuffle(temp, GameShell.rand);
			i=0;
			for (int x=0; x<GRID_EXTENT_X && i<MAX; ++x) {
				for (int y=0; y<GRID_EXTENT_Y && i<MAX; ++y) {
					for (int z=0; z<GRID_EXTENT_Z && i<MAX; ++z) {
						if (layout.cell_matrix[x][y][z]) {
							cells[x][y][z] = temp[i];
							++i;
						}
					}
				}
			}
			updateRemainingMoveCount();
			initial_matching_pair_count = remainingMoveCount;
		} while (initial_matching_pair_count<MIN_MATCHING_PAIRS);
		
		// Increase started count
		Integer started = Preferences.get(AceMahjongConstants.KEY_GAMES_STARTED);
		if (started==null) started = 0;
		started++;
		Preferences.set(AceMahjongConstants.KEY_GAMES_STARTED, started);
		if (!RegistrationData.has_registered()) {
			if ((started%5)==0) {
				nag = true;
			}
		}
	}
	
	Renderable scene = new Renderable() {
		public void render(GL gl_in) {
			enter_perspective_mode(gl_in);
			
			// Ground plane
			gl_in.glLoadIdentity();
			gl_in.glTranslatef(0, -2f*OVERALL_SCALE*SCALE_Y, -7f*SCALE_Z);
			gl_in.glColor3fv(KSGraphicsConstants.GL_COLOUR_4F_WHITE, 0);
			gl_in.glScalef(5, 6.5f, 4.7f);
			ground_plane.render(gl_in);
			
			gl_in.glLoadIdentity();
			
			final float xoff = -GRID_EXTENT_X/2;
			final float yoff = -2-GRID_EXTENT_Y/2;
			
	    	int i=0;
			for (int z=0; z<GRID_EXTENT_Z; ++z) { // Render most distant first, so that z-buffering interacts correcting with alpha-blending
				for (int y=GRID_EXTENT_Y-1; y>=0; --y) {
					for (int x=GRID_EXTENT_X-1; x>=0; --x) {
						final Tile this_cell = cells[x][y][z];
						if (this_cell!=null) {
							this_cell.scale = OVERALL_SCALE;
							this_cell.xpos = (x+xoff)*SCALE_X*OVERALL_SCALE;
							this_cell.ypos = (y+yoff)*SCALE_Y*OVERALL_SCALE;
							this_cell.zpos = z*SCALE_Z*OVERALL_SCALE;
							
							float col[] = KSGraphicsConstants.GL_COLOUR_4F_WHITE;
							this_cell.render(gl_in, this_cell.enabled? col : this_cell.model_colour_disabled);
						}
						++i;
					}
				}
			}
			
			exit_perspective_mode(gl_in);
		}
	};
	
	Renderable shadows = new Renderable() {
		public void render(GL gl_in) {
			enter_perspective_mode(gl_in);
			
			// Ground plane
			gl_in.glLoadIdentity();
			gl_in.glTranslatef(0, -2f*OVERALL_SCALE*SCALE_Y, -7f*SCALE_Z);
			gl_in.glColor4f(0, 0, 0, 1);
			gl_in.glScalef(5, 6.5f, 4.7f);
//			ground_plane.render(gl_in);
			
			gl_in.glLoadIdentity();
			
			final float xoff = -GRID_EXTENT_X/2;
			final float yoff = -2-GRID_EXTENT_Y/2;
			
	    	int i=0;
			for (int z=0; z<GRID_EXTENT_Z; ++z) { // Render most distant first, so that z-buffering interacts correcting with alpha-blending
				for (int y=GRID_EXTENT_Y-1; y>=0; --y) {
					for (int x=GRID_EXTENT_X-1; x>=0; --x) {
						final Tile this_cell = cells[x][y][z];
						if (this_cell!=null) {
							this_cell.scale = OVERALL_SCALE;
							this_cell.xpos = (x+xoff)*SCALE_X*OVERALL_SCALE;
							this_cell.ypos = (y+yoff)*SCALE_Y*OVERALL_SCALE;
							this_cell.zpos = z*SCALE_Z*OVERALL_SCALE;
							
							this_cell.render_shadow(gl_in);
						}
						++i;
					}
				}
			}
			
			exit_perspective_mode(gl_in);
		}
	};
	
	public void render(GL gl) {
		gl.glClearStencil(0);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, new float[]{5000,-10000,30000,0}, 0);
		gl.glLoadIdentity();
		
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LESS);
		
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_LIGHT0);
		gl.glDisable(GL.GL_BLEND);
		gl.glShadeModel(GL.GL_FLAT);
		gl.glColor4d(1, 1, 1, 1);
		
		gl.glDepthMask(false);
		gl.glDisable(GL.GL_DEPTH_TEST);
		menu_background.render(gl);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		
		// Lit stuff
		renderLayout(gl);
		
		gl.glDisable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_BLEND);
		gl.glDisable(GL.GL_STENCIL_TEST);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		help.ypos = help.height/2 + ((-GameShell.display_region_height)/2);
		help.render(gl);
		
		// Buttons
		gl.glEnable(GL.GL_LIGHTING);
		float	x2 = -GameShell.display_region_width/2,
				y1 = GameShell.display_region_height/2,
				x1 = x2+28,
				y2 = (float)get_hud_box_bottom();
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glLoadIdentity();
		gl.glEnable(GL.GL_BLEND);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glColor4f(0, 0, 0, 0.5f);
		gl.glRectd(-x2, y1, x2, y2);
		
		gl.glEnable(GL.GL_LIGHTING);
		final float HUD_BUTTON_Y = (-pause.height/2)+(y1+y2)/2;
		final float FAR = -(shuffle.width*shuffle.scale)+GameShell.display_region_width/2;
		final float SPACE = 0.5f*shuffle.scale;
		shuffle.xpos = FAR - shuffle.width;
		shuffle.ypos = HUD_BUTTON_Y;
		shuffle.setText("Shuffle ("+remaining_shuffles+")", false);
		shuffle.render(gl);
		hint.xpos = shuffle.xpos - (SPACE+hint.width*shuffle.scale);
		hint.ypos = HUD_BUTTON_Y;
		hint.setText("Hint ("+remaining_hints+")", false);
		hint.render(gl);
		pause.xpos = hint.xpos - (SPACE+pause.width*shuffle.scale);
		pause.ypos = HUD_BUTTON_Y;
		pause.render(gl);
		
		final float BUTTON_SIZE = rotClockwise.height*5f;
		final float CAMERA_BUTTON_MIDDLE_Y = y2 - (1.4f*BUTTON_SIZE);
		final float CAMERA_BUTTON_MIDDLE_X = (1.4f*BUTTON_SIZE) - GameShell.display_region_width/2;
		gl.glLoadIdentity();
		gl.glTranslatef(CAMERA_BUTTON_MIDDLE_X, CAMERA_BUTTON_MIDDLE_Y, 0);
		gl.glScalef(BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
		cameraLabelImage.render(gl);
		rotClockwise.setPosByCentre(		CAMERA_BUTTON_MIDDLE_X-BUTTON_SIZE, CAMERA_BUTTON_MIDDLE_Y	);
		rotAnticlockwise.setPosByCentre(	CAMERA_BUTTON_MIDDLE_X+BUTTON_SIZE, CAMERA_BUTTON_MIDDLE_Y	);
		rotUp.setPosByCentre(			CAMERA_BUTTON_MIDDLE_X,				CAMERA_BUTTON_MIDDLE_Y+BUTTON_SIZE	);
		rotDown.setPosByCentre(			CAMERA_BUTTON_MIDDLE_X,				CAMERA_BUTTON_MIDDLE_Y-BUTTON_SIZE	);
		rotClockwise.render(gl);
		rotAnticlockwise.render(gl);
		rotUp.render(gl);
		rotDown.render(gl);
		
		if (selected_cell!=null) {
			// Go into sort-of-isometric view
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glMultTransposeMatrixd(DISPLAY_MATRIX, 0); // OpenGL is transposed relative to normal matrix writing style
			gl.glMatrixMode(GL.GL_MODELVIEW);
			// Render the tile
			int id = selected_cell.get_id();
			gl.glLoadIdentity();
			gl.glTranslated(0, (y1+y2)/2, 0);
			final float S = 4;
			gl.glScaled(S, S, S);
			float opacity = (float)Math.sin(2f*game_tick/GameShell.TARGET_FPS);
			opacity += 1; opacity /= 2;
			opacity /= 2;
			opacity += 0.2;
			gl.glColor4d(1, 1, 1, opacity);
			gl.glEnable(GL.GL_CULL_FACE);
			tile_models[id].render(gl);
			// Return to normal view
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPopMatrix();
			gl.glMatrixMode(GL.GL_MODELVIEW);
		}
		
		// HUD
		// All text
		float col[]=KSGraphicsConstants.GL_COLOUR_4F_WHITE;
		float y = GameShell.display_region_height/2;
		float x = -GameShell.display_region_width/2;
		float delta = font_small.get_default_line_spacing()*0.75f;
		x += delta;
		x += 17;
		y -= delta*0.5;
		y -= delta;
		font_small.draw_right("Tiles remaining: ", col, x, y);
		font_small.draw(" "+count_remaining_tiles(), col, x, y);		
		x += 25;
		font_small.draw_right("Moves remaining: ", col, x, y);
		if (remainingMoveCount==0 && count_remaining_tiles()!=0) font_small.draw(" 0 (use shuffle!)", KSGraphicsConstants.GL_COLOUR_4F_RED, x, y);
		else font_small.draw(" "+remainingMoveCount, col, x, y);
		x -= 10;
		y -= delta/4;
		delta = font_menu_option.get_default_line_spacing()*0.75f;
		y -= delta;
		font_menu_option.draw_right("Time taken: ", col, x, y);
		font_menu_option.draw(" "+FormatSecondsAsStopwatchTime(game_tick/GameShell.TARGET_UPS), col, x, y);
		y -= delta;
		font_menu_option.draw_right("Score: ", col, x, y);
		font_menu_option.draw(" "+score, col, x, y);
		y -= delta;
		
		if (DEBUG) {
			font_menu_option.draw_right("FPS: "+GameShell.actual_fps, col, x, y);
		}
		
		if (nag) {
			gl.glDisable(GL.GL_LIGHTING);
			FullScreenEffects.fadeToColour(0, 0, 0, 0.5f);
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glEnable(GL.GL_LIGHTING);
			gl.glEnable(GL.GL_LIGHT0);
			gl.glShadeModel(GL.GL_SMOOTH);
			gl.glEnable(GL.GL_BLEND);
			
			float c[] = new float[]{1,1,1,0.75f};
			nag_dialog.setThisModelColours(c, c, c);
			
			float chover[] = new float[]{0.3f, 1, 1, 0.85f};
			double phase = 4*GameShell.logic_tick/((double)GameShell.TARGET_UPS);
			float ct[] = new float[]{1, 1, 1, 0.9f+0.1f*((float)Math.sin(phase))};
			float cmodel[] = new float[]{0.8f, 0.8f, 0.8f, 0.85f};
			for (Button b : nag_dialog.buttons) {
				b.setThisModelColours(cmodel, chover, cmodel);
				b.setThisTextColours(ct, ct, ct);
			}
			nag_dialog.render(gl);
		}
		
		gl.glEnable(GL.GL_BLEND);
		gl.glDisable(GL.GL_LIGHTING);
	}

	private float get_hud_box_bottom() {
		float y = GameShell.display_region_height/2;
		return y-HUD_HEIGHT;
	}
	
	public void logic() {
		if (nag) {
			nag_dialog.update();
			if (nag_dialog.button_clicked==0) nag = false;
			else if (nag_dialog.button_clicked==1) {
				nag = false;
				MenuSystem.target_menu = MiscGlobals.MENU_REGISTER;
			}
			return;
		}
		
		super.logic();
		
		final float ROT_SPEED = 0.03f;
		if (KeyboardBuffer.keyHeld[KeyEvent.VK_LEFT]) cameraYaw -= ROT_SPEED;
		if (KeyboardBuffer.keyHeld[KeyEvent.VK_RIGHT]) cameraYaw += ROT_SPEED;
		if (KeyboardBuffer.keyHeld[KeyEvent.VK_UP]) cameraPitch += ROT_SPEED;
		if (KeyboardBuffer.keyHeld[KeyEvent.VK_DOWN]) cameraPitch -= ROT_SPEED;
		
		rotAnticlockwise.update(); if (rotAnticlockwise.mouse_lmb_held) cameraYaw += ROT_SPEED;
		rotClockwise.update(); if (rotClockwise.mouse_lmb_held) cameraYaw -= ROT_SPEED;
		rotUp.update(); if (rotUp.mouse_lmb_held) cameraPitch += ROT_SPEED;
		rotDown.update(); if (rotDown.mouse_lmb_held) cameraPitch -= ROT_SPEED;
		
		final float MIN_PITCH = KSMath.PI_OVER_2 * 0.1f;
		final float MAX_PITCH = KSMath.PI_OVER_2 * 0.9f;
		if (cameraPitch<MIN_PITCH) cameraPitch = MIN_PITCH;
		else if (cameraPitch>MAX_PITCH) cameraPitch = MAX_PITCH;
		cameraDistance += MouseBuffer.getBufferedScrollwheelDelta();
		if (cameraDistance<64) cameraDistance = 64;
		else if (cameraDistance>255) cameraDistance = 255;
		positionCamera();
		
		do_endgame_check();
		
		pause.update();
		if (pause.something_happened) {
			if (pause.mouse_lmb_click) {
				SoundSystem.playWave(click);
			}
		}
		hint.update();
		if (hint.something_happened) {
			if (hint.mouse_lmb_click) {
				SoundSystem.playWave(click);
				hint();
			}
		}
		shuffle.update();
		if (shuffle.something_happened) {
			if (shuffle.mouse_lmb_click) {
				SoundSystem.playWave(click);
				shuffle();
			}
		}
		
		// Clicking on tiles
		tile_has_been_clicked_on = false;
		for (int x=0; x<GRID_EXTENT_X; ++x) {
			for (int y=0; y<GRID_EXTENT_Y; ++y) {
				for (int z=0; z<GRID_EXTENT_Z; ++z) {
					Tile t = cells[x][y][z]; 
					if (t!=null) {
						if (t.fade_out()) { cells[x][y][z]=null; continue; }
						t.enabled = !cell_is_blocked(x, y, z);
						if (t.enabled) {
							t.update();
							if (t.something_happened && t.mouse_lmb_click) cell_click(x, y, z);
						}
					}
				}
			}
		}
		if (MouseBuffer.buttons_clicked!=0 && !tile_has_been_clicked_on) {
			selected_cell = null;
			for (int x=0; x<GRID_EXTENT_X; ++x) {
				for (int y=0; y<GRID_EXTENT_Y; ++y) {
					for (int z=0; z<GRID_EXTENT_Z; ++z) {
						Tile t = cells[x][y][z]; 
						if (t!=null) {
							t.highlighted = false;
						}
					}
				}
			}
		}
	}
	
	private void shuffle() {
		if (remaining_shuffles<=0) {
			GameShell.messages.add(new SelfFadingMessage(0, 0, NO_MORE_SHUFFLES_THIS_GAME, font_menu_option));
			return;
		}
		--remaining_shuffles;
		no_shuffles_used = false;
		int tries=0;
		final int MAX_TRIES = 200;
		do {
			do_shuffle();
			++tries;
			updateRemainingMoveCount();
		} while (remainingMoveCount==0 && tries<MAX_TRIES);
	}
	
	private void do_shuffle() {
		Integer old_ids[]=new Integer[144];
		int tile_count = 0;
		for (int x=0; x<GRID_EXTENT_X; ++x) {
			for (int y=0; y<GRID_EXTENT_Y; ++y) {
				for (int z=0; z<GRID_EXTENT_Z; ++z) {
					Tile t = cells[x][y][z]; 
					if (t!=null) {
						t.highlighted = false;
						old_ids[tile_count] = t.get_id();
						tile_count++;
					}
				}
			}
		}
		Integer short_array[] = new Integer[tile_count];
		System.arraycopy(old_ids, 0, short_array, 0, tile_count);
		ArrayUtil.shuffle(short_array, GameShell.rand);
		tile_count = 0;
		for (int x=0; x<GRID_EXTENT_X; ++x) {
			for (int y=0; y<GRID_EXTENT_Y; ++y) {
				for (int z=0; z<GRID_EXTENT_Z; ++z) {
					Tile t = cells[x][y][z]; 
					if (t!=null) {
						t.set_id(short_array[tile_count]);
						t.highlighted = false;
						t.hinted = false;
						if (++tile_count>short_array.length) return;
					}
				}
			}
		}		
	}

	private void hint() {
		if (remaining_hints<=0) {
			GameShell.messages.add(new SelfFadingMessage(0, 0, NO_MORE_HINTS_THIS_GAME, font_menu_option));
			return;
		}
		
		int x1, y1, z1, x2, y2, z2;
		
		if (remainingMoveCount==0) {
			GameShell.messages.add(new SelfFadingMessage(0, 0, NO_MOVES_REMAINING_TRY_ZAPPING_OR_CHEATING, font_menu_option));
		}
		else if (there_are_hinted_tiles()) {
			float x = 0;
			float y = small_font_msg_y_pos();
			SelfFadingMessage sfm = new SelfFadingMessage(x, y, CLICK_THE_FLASHING_TILES, font_small);
			GameShell.messages.add(sfm);
		}
		else {
			remaining_hints--; no_hints_used = false;
			for (x1=0; x1<GRID_EXTENT_X; x1++) {
				for (y1=0; y1<GRID_EXTENT_Y; y1++) {
					for (z1=0; z1<GRID_EXTENT_Z; z1++) {
						
						if (cell_exists(x1, y1, z1)) {
							Tile tile_1 = cells[x1][y1][z1];
							
							for (x2=0; x2<GRID_EXTENT_X; x2++) {
								for (y2=0; y2<GRID_EXTENT_Y; y2++) {
									for (z2=0; z2<GRID_EXTENT_Z; z2++) {
										Tile tile_2 = cells[x2][y2][z2];
										if (cell_exists(x2, y2, z2) && tile_1.compare_id(tile_2)) {
											if (can_remove_pair(x1, y1, z1, x2, y2, z2)) {
												tile_1.hinted = tile_2.hinted = true;
												GameShell.messages.add(new SelfFadingMessage(0, small_font_msg_y_pos(), CLICK_THE_FLASHING_TILES, font_small));
												x1 = x2 = GRID_EXTENT_X;
												y1 = y2 = GRID_EXTENT_Y;
												return;
											}
										}
									}
								}
							}
							
						}
						
					}
				}
			}
		}
	}

	private float small_font_msg_y_pos() {
		return get_hud_box_bottom()-font_small.get_default_line_spacing();
	}
	
	private boolean there_are_hinted_tiles() {
		for (int x1=0; x1<GRID_EXTENT_X; x1++) {
			for (int y1=0; y1<GRID_EXTENT_Y; y1++) {
				for (int z1=0; z1<GRID_EXTENT_Z; z1++) {
					if (cells[x1][y1][z1]!=null && cells[x1][y1][z1].hinted) return true;
				}
			}
		}
		return false;
	}
	
	public void game_over(boolean winner) {
		int total_score;
		if (winner) {
			score_bonus_time = calculate_time_bonus();
			total_score = score+score_bonus_hints+score_bonus_shuffles+score_bonus_time;
		}
		else {
			score_bonus_time = 0;
			total_score = score;
		}
		test_score_achievements(total_score);
		GenericHighscoreEntryMenuScreen.last_highscore =
			new HighscoreEntry(
					GenericHighscoreEntryMenuScreen.input.getText(true),
					total_score,
					0, 0,
					ResourceManager.layouts[ResourceManager.selected_layout].hash,
					winner,
					new Date());
		HighscoreEntryMenuScreen.set_display_scores();
		delete_save_game();
		
		// Achievement-related stuff
		Integer games_played = Preferences.get(KEY_GAMES_PLAYED);
		if (games_played==null) games_played = 0;
		games_played++;
		Preferences.set(KEY_GAMES_PLAYED, games_played);
		if (games_played==1) Achievements.award_achivement(ACHIEVEMENT_PURPOSEFUL_KITSUNE);
		else if (games_played==5) Achievements.award_achivement(ACHIEVEMENT_DETERMINED_KITSUNE);
		else if (games_played==50) Achievements.award_achivement(ACHIEVEMENT_DOGGED_KITSUNE);
		else if (games_played==500) Achievements.award_achivement(ACHIEVEMENT_PERSISTENT_KITSUNE);
		else if (games_played==5000) Achievements.award_achivement(ACHIEVEMENT_TIRELESS_KITSUNE);
		
		layouts[selected_layout].complete_a_game();
		
		MenuSystem.target_menu = MENU_HIGHSCORE_ENTRY_SCREEN;
	}
	
	private int calculate_time_bonus() {
		// TODO Auto-generated method stub
		float seconds = ((float)game_tick)/GameShell.TARGET_FPS;
		float minutes = seconds/60;
		return (int)( 20000 * Math.pow(10, minutes/10) );
	}
	
	public void increase_score(int points) {
		score += points;
		test_score_achievements(score);
	}
	
	private void test_score_achievements(int score_in) {
		if (score_in>=10000) Achievements.award_achivement(ACHIEVEMENT_DECENT_SCORE);
		if (score_in>=25000) Achievements.award_achivement(ACHIEVEMENT_GREAT_SCORE);
		if (score_in>=50000) Achievements.award_achivement(ACHIEVEMENT_AWESOME_SCORE);
		if (score_in>=100000) Achievements.award_achivement(ACHIEVEMENT_ULTIMATE_SCORE);
	}
	
	private void cell_click(int x_in, int y_in, int z_in) {
		if (cell_is_blocked(x_in, y_in, z_in)) return;
		
		if (x_in>=0 && y_in>=0 && x_in<GRID_EXTENT_X && y_in<GRID_EXTENT_Y && cell_exists(x_in, y_in, z_in)) {
			if (1==selected_cell_count()) { // If one is already selected before this click
				if (cells[x_in][y_in][z_in] == selected_cell) { // The one already selected was this one
					cells[x_in][y_in][z_in].highlighted = !cells[x_in][y_in][z_in].highlighted;
					if (!selected_cell.highlighted) {
						selected_cell = null;
					}
					SoundSystem.playWave(click);
				}
				else if (!cell_is_blocked(x_in, y_in, z_in)) {
					if (cells[x_in][y_in][z_in].compare_id(selected_cell)) {
						cells[x_in][y_in][z_in].highlighted = true;
						try_to_remove_cell_pair();
						if (cell_exists(x_in, y_in, z_in)) { // Remove failed
							cells[x_in][y_in][z_in].highlighted = false;
						}
					}
					else {
						// Tiles do not match
						GameShell.messages.add(new SelfFadingMessage(0, small_font_msg_y_pos(), TILES_DO_NOT_MATCH, font_small));
					}
				}
				else {
					GameShell.messages.add(new SelfFadingMessage(0, small_font_msg_y_pos(), TILE_IS_BLOCKED, font_small));
				}
			}
			else if (0==selected_cell_count()) { // If nothing is already selected
				if (!cell_is_blocked(x_in, y_in, z_in)) {
					selected_cell = cells[x_in][y_in][z_in];
					selected_cell.highlighted = true;
					SoundSystem.playWave(click);
				}
				else {
					GameShell.messages.add(new SelfFadingMessage(0, small_font_msg_y_pos(), TILE_IS_BLOCKED, font_small));
				}
			}
			do_endgame_check();
			tile_has_been_clicked_on = true;
		}
		else {
			// This probably shouldn't ever occur... but if it does, I think it means there was no cell at that location.
		}
	}
	
	private void do_endgame_check() {
		if (count_remaining_tiles()==0) {
			win();
		}
		else if (0==remainingMoveCount && 0==remaining_shuffles) {
			lose();
		}
	}
	
	public void win() {
		SoundSystem.playWave(win);
		// Win count and related achievements
		Integer games_won = Preferences.get(KEY_GAMES_WON);
		if (games_won==null) games_won = 0;
		games_won++;
		Preferences.set(KEY_GAMES_WON, games_won);
		if (games_won==1) Achievements.award_achivement(ACHIEVEMENT_CLEVER_KITSUNE);
		else if (games_won==10) Achievements.award_achivement(ACHIEVEMENT_CUNNING_KITSUNE);
		else if (games_won==25) Achievements.award_achivement(ACHIEVEMENT_CRAFTY_KITSUNE);
		else if (games_won==100) Achievements.award_achivement(ACHIEVEMENT_CANNY_KITSUNE);
		else if (games_won==1000) Achievements.award_achivement(ACHIEVEMENT_CALCULATING_KITSUNE);
		else if (games_won==2000) Achievements.award_achivement(ACHIEVEMENT_INDEFATIGABLE_KITSUNE);
		
		// Multi-layout victory achievements
		layouts[selected_layout].win_a_game();
		
		// Wins-in-a-row count and related achievements
		Integer games_won_in_a_row = Preferences.get(KEY_GAMES_WON_IN_A_ROW);
		if (games_won_in_a_row==null) games_won_in_a_row = 0;
		if (games_won_in_a_row<=0) games_won_in_a_row = 1; // If we were on a losing streak, we are now on a winning streak 
		else games_won_in_a_row++; // If we were on a winning streak (including null streak), it just got better
		Preferences.set(KEY_GAMES_WON_IN_A_ROW, games_won_in_a_row);
		if (games_won_in_a_row==3) Achievements.award_achivement(ACHIEVEMENT_RELIABLE_VICTORY);
		else if (games_won_in_a_row==7) Achievements.award_achivement(ACHIEVEMENT_DEPENDABLE_VICTORY);
		else if (games_won_in_a_row==11) Achievements.award_achivement(ACHIEVEMENT_CONSTANT_VICTORY);
		else if (games_won_in_a_row==17) Achievements.award_achivement(ACHIEVEMENT_UNFAILING_VICTORY);
		
		// Time taken achievements
		int seconds_taken = game_tick/GameShell.TARGET_UPS;
		int minutes_taken = seconds_taken/60;
		if (minutes_taken<15) Achievements.award_achivement(ACHIEVEMENT_FAST_FOX);
		if (minutes_taken<10) Achievements.award_achivement(ACHIEVEMENT_QUICK_BROWN_FOX);
		if (minutes_taken<5) Achievements.award_achivement(ACHIEVEMENT_FASTEST_FOX_IN_THE_WEST);
		
		// Win-without-using-X achievements
		if (no_hints_used) Achievements.award_achivement(ACHIEVEMENT_OVERSIGHT);
		if (no_shuffles_used) Achievements.award_achivement(ACHIEVEMENT_PLAYING_FAIR);
		
		// Win a difficult game
		if (initial_matching_pair_count<10) Achievements.award_achivement(ACHIEVEMENT_WIND_OF_CHANGE);
		
		// Unused helper bonuses
		score_bonus_hints = 800 * remaining_hints;
		score_bonus_shuffles = 1200 * remaining_shuffles;
		game_over(true);
	}
	
	public void lose() {
		SoundSystem.playWave(lose);
		Integer wins_in_a_row = Preferences.get(KEY_GAMES_WON_IN_A_ROW);
		if (wins_in_a_row==null) wins_in_a_row = 0;
		if (wins_in_a_row>=0) Preferences.set(KEY_GAMES_WON_IN_A_ROW, -1); // If we were on a winning streak, we are now on a losing streak 
		else Preferences.set(KEY_GAMES_WON_IN_A_ROW, wins_in_a_row-1); // If we were on a losing streak (including null streak), it just got worse
		game_over(false);
	}
	
	private int remainingMoveCount = 0;
	private void updateRemainingMoveCount() {
		int result = 0;
		
		for (int fromX=0; fromX<GRID_EXTENT_X; ++fromX) {
			for (int fromY=0; fromY<GRID_EXTENT_Y; ++fromY) {
				for (int fromZ=0; fromZ<GRID_EXTENT_Z; ++fromZ) {
					
					if (cell_exists(fromX, fromY, fromZ)) {
						Tile from = cells[fromX][fromY][fromZ];
						
						for (int toX=0; toX<GRID_EXTENT_X; ++toX) {
							for (int toY=0; toY<GRID_EXTENT_Y; ++toY) {
								for (int toZ=0; toZ<GRID_EXTENT_Z; ++toZ) {
								
									if (	(fromX!=toX || fromY!=toY || fromZ!=toZ) &&
											 cell_exists(toX, toY, toZ) &&
											cells[toX][toY][toZ].compare_id(from)) {
										if (can_remove_pair(fromX, fromY, fromZ, toX, toY, toZ)) ++result;
									}
								}
							}
						}
						
					}
					
				}
			}
		}
		
		remainingMoveCount = result/2; // Need to /2 because it counts a->b and b->a as unique
	}
	
	private int count_remaining_tiles() {
		int count=0;
		
		for (int x=0; x<GRID_EXTENT_X; ++x) {
			for (int y=0; y<GRID_EXTENT_Y; ++y) {
				for (int z=0; z<GRID_EXTENT_Z; ++z) {
					if (cell_exists(x, y, z)) ++count;
				}
			}
		}
		return count;
	}
	
	private boolean cell_exists(int x, int y, int z) {
		return cells[x][y][z]!=null && cells[x][y][z].opacity==0x7F;
	}
	
	private void try_to_remove_cell_pair() {
		int x1, y1, z1, x2, y2, z2;
		boolean someCellsRemovedFlag = false;
		
		x1 = -1;
		x2 = -1;
		y1 = -1;
		y2 = -1;
		z1 = -1;
		z2 = -1;
		
		for (int x=0; x<GRID_EXTENT_X; ++x) {
			for (int y=0; y<GRID_EXTENT_Y; ++y) {
				for (int z=0; z<GRID_EXTENT_Z; ++z) {
					if (cell_exists(x, y, z) && cells[x][y][z].highlighted) {
						if (x1<0) {
							x1 = x;
							y1 = y;
							z1 = z;
						}
						else {
							x2 = x;
							y2 = y;
							z2 = z;
						}
					}
				}
			}
		}
		
		if (can_remove_pair(x1, y1, z1, x2, y2, z2)) {
			
			for (int x=0; x<GRID_EXTENT_X; ++x) {
				for (int y=0; y<GRID_EXTENT_Y; ++y) {
					for (int z=0; z<GRID_EXTENT_Z; ++z) {
						if (cell_exists(x, y, z) && cells[x][y][z].highlighted) {
							cells[x][y][z].opacity = 0x7E;
							cell_removed(x, y, z);
							someCellsRemovedFlag = true;
						}
					}
				}
			}
		}
		
		if (someCellsRemovedFlag) {
			some_cells_removed(true);
		}
	}
	
	private boolean can_remove_pair(int x1, int y1, int z1, int x2, int y2, int z2) {
		Tile t1 = cells[x1][y1][z1], t2 = cells[x2][y2][z2];
		if (t1==null || t2==null || t1==t2) return false;
		if (!t1.compare_id(t2)) return false;
		if (cell_is_blocked(x1, y1, z1)) return false;
		if (cell_is_blocked(x2, y2, z2)) return false;
		return true;
	}
	
	private boolean cell_is_blocked(int x, int y, int z) {
		int min_y = y-1, max_y = y+1;
		int min_x = x-2, max_x = x+2;
		if (min_y<0) min_y = 0;
		if (max_y>=GRID_EXTENT_Y) min_y = GRID_EXTENT_Y-1;
		// Check left
		boolean blocked_left = false;
		if (min_x>=0) {
			for (int yp=min_y; yp<=max_y && !blocked_left; ++yp) {
				if (cell_exists(min_x, yp, z)) blocked_left = true;
			}
		}
		else min_x=0;
		// Check right
		if (max_x<GRID_EXTENT_X) {
			for (int yp=min_y; yp<=max_y; ++yp) {
				if (cell_exists(max_x, yp, z) && blocked_left) return true;
			}
		}
		else max_x=GRID_EXTENT_X-1;
		// Check up
		z++;
		min_x = x-1; if (min_x<0) min_x=0;
		max_x = x+2; if (max_x>=GRID_EXTENT_X) max_x=GRID_EXTENT_X-1;
		if (z<GRID_EXTENT_Z) {
			for (int xp=min_x; xp<max_x; ++xp) {
				for (int yp=min_y; yp<=max_y; ++yp) {
					if (cell_exists(xp, yp, z)) return true;					
				}
			}
		}
		// No block found!
		return false;
	}
	
	private void some_cells_removed(boolean show_path) {
		SoundSystem.playWave(swipe);
		selected_cell = null;
		updateRemainingMoveCount();
		if (!show_path) return;
	}
	
	private void cell_removed(int cell_x, int cell_y, int cell_z) {
		increase_score(500);
		shadow_map_dirty = true;
	}
	
	private int selected_cell_count() {
		int result = 0;
		
		for (int z=0; z<GRID_EXTENT_Z; ++z) {
			for (int y=0; y<GRID_EXTENT_Y; ++y) {
				for (int x=0; x<GRID_EXTENT_X; ++x) {
					if (cell_exists(x, y, z) && cells[x][y][z].highlighted) ++result;
				}
			}
		}
		return result;
	}
	
	public void enter_perspective_mode(GL gl_in) {
		gl_in.glLoadIdentity();
		gl_in.glMatrixMode(GL.GL_PROJECTION);
		gl_in.glPushMatrix();
		KSGLUtil.enter_perspective_mode(gl_in, 45, 1, 500);
		camera.focus();
		gl_in.glMatrixMode(GL.GL_MODELVIEW);
	}
	
	public void exit_perspective_mode(GL gl_in) {
		gl_in.glMatrixMode(GL.GL_PROJECTION);
		gl_in.glPopMatrix();
		gl_in.glMatrixMode(GL.GL_MODELVIEW);
	}
	
	public void renderLayout(GL gl) {
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
		gl.glLightModelf(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
		scene.render(gl);
		
		gl.glDisable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_STENCIL_TEST);
//		gl.glStencilFunc( GL.GL_EQUAL, 0, 0xFFFFFFFF ); // Render scene where the stencil is zero (unshadowed).
//		shadows.render(gl);
		
//		gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
//		gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
//		
//		//1. Disable writes to the depth and colour buffers.
//		gl.glDepthMask(false);
//		gl.glColorMask(false, false, false, false);
//		//2. Use back-face culling.
//		gl.glEnable(GL.GL_CULL_FACE);
//		gl.glCullFace(GL.GL_FRONT);
//		//3. Set the stencil operation to increment on depth pass (only count shadows in front of the object).
//		gl.glEnable(GL.GL_STENCIL_TEST);
//		gl.glStencilFunc( GL.GL_ALWAYS, 1, 0xFFFFFFFF );
//		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_INCR);
//		//4. Render the shadow volumes (because of culling, only their front faces are rendered).
//		shadows.render(gl);
//		//5. Use front-face culling.
//		gl.glCullFace(GL.GL_BACK);
//		//6. Set the stencil operation to decrement on depth pass.
//		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_DECR);
//		//7. Render the shadow volumes (only their back faces are rendered).
//		shadows.render(gl);
//		
//		gl.glStencilFunc( GL.GL_NOTEQUAL, 0, 0xFFFFFFFF ); // Render scene where the stencil is non-zero (shadowed).
//		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
//		gl.glEnable(GL.GL_STENCIL_TEST);
//		gl.glColorMask(true, true, true, true);
//		gl.glDisable(GL.GL_CULL_FACE);
//		FullScreenEffects.fadeToColour(0, 0, 0, 0.75f);
//		
//		gl.glPopAttrib();
	}
	
}
