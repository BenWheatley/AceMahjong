package game.menu;

import static game.ResourceManager.font_title;
import static game.ResourceManager.tile_model;

import game.MahjongLayout;

import java.awt.Color;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.graphics.KSGraphicsConstants;
import kslib.graphics._3d.util.KSGLUtil;
import kslib.io.MouseBuffer;
import kslib.ui.SelfFadingMessage;
import kslib.ui.menu.BasicEditorMenuScreen;
import kslib.ui.menu.menu_options.MenuOption;
import kslib.util.Renderable;

import static game.MahjongLayout.*;
import static game.ResourceManager.*;

import static game.AceMahjongConstants.*;

public class EditorMenuScreen extends BasicEditorMenuScreen {
	
	private SelfFadingMessage editor_help, tile_count_msg;
	private float last_render_matrix[][][][] = new float[GRID_EXTENT_X][GRID_EXTENT_Y][GRID_EXTENT_Z][16];
	
	private int place_mouse_cell_x = -1, place_mouse_cell_y = -1, place_mouse_cell_z = -1;
	private int remove_mouse_cell_x = -1, remove_mouse_cell_y = -1, remove_mouse_cell_z = -1;
	
	private int last_left_click_layer = -1, last_right_click_layer = -1;
	
	public EditorMenuScreen(Renderable background_in, MenuOption[] options_in, int default_h_alignment, int default_v_alignment) {
		super(background_in, options_in, default_h_alignment, default_v_alignment);
		the_level = new MahjongLayout();
	}
	
	public void render(GL gl) {
		gl.glDisable(GL.GL_STENCIL_TEST);
		gl.glDisable(GL.GL_CULL_FACE);
		gl.glDepthMask(true);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		super.render(gl);
		
		float draw_y = 3*GameShell.display_region_height/8;
		font_title.draw_multiline("Layout Editor",
				KSGraphicsConstants.GL_COLOUR_4F_WHITE,
				0, draw_y,
				0, 0,
				KSGraphicsConstants.ALIGN_H_CENTRE, KSGraphicsConstants.ALIGN_V_MIDDLE,
				0,
				false);
		
		final float SCALE_UP = 3;
		final float xoff = -(GRID_EXTENT_X+1)/2f;
		final float yoff = -(GRID_EXTENT_Y+2)/2f;
		
		place_mouse_cell_x = -1; place_mouse_cell_y = -1; place_mouse_cell_z = -1;
		remove_mouse_cell_x = -1; remove_mouse_cell_y = -1; remove_mouse_cell_z = -1;
		
		boolean cell_matrix[][][] = ((MahjongLayout)the_level).cell_matrix;
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glMultTransposeMatrixd(DISPLAY_MATRIX, 0); // OpenGL is transposed relative to normal matrix writing style
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		int x0 = 0, y0 = 0, z0 = 0,
			xm = GRID_EXTENT_X-1, ym = GRID_EXTENT_Y-1, zm = GRID_EXTENT_Z;
		draw_grid(gl, SCALE_UP, xoff, yoff, x0, y0, xm, ym);
		
		for (int z=0; z<GRID_EXTENT_Z; ++z) {
			Color c = get_level_colour(z, 1, 0.5f);
			for (int y=0; y<GRID_EXTENT_Y; y++) {
				for (int x=0; x<GRID_EXTENT_X; x++) {
					if (can_place_tile_here(x, y, z) || cell_matrix[x][y][z]) {
						float alpha = 1;
						if (!cell_matrix[x][y][z]) {
							alpha = 0.05f;
						}
						float col2[] = {c.getRed()/256f, c.getGreen()/256f, c.getBlue()/256f, alpha};
						gl.glColor4fv(col2, 0);
						gl.glLoadIdentity();
						gl.glScaled(SCALE_UP, SCALE_UP, 1);
						gl.glTranslated((x+xoff)*SCALE_X, (y+yoff)*SCALE_Y, z);
						if (cell_matrix[x][y][z]) tile_model.render(gl);
						gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, last_render_matrix[x][y][z], 0);
						if (KSGLUtil.test_collide_with_mouse(gl, tile_model)) {
							if (cell_matrix[x][y][z]) {
								remove_mouse_cell_x = x;
								remove_mouse_cell_y = y;
								remove_mouse_cell_z = z;
							}
							else {
								place_mouse_cell_x = x;
								place_mouse_cell_y = y;
								place_mouse_cell_z = z;
							}
						}
					}
				}
			}
		}
		if (place_mouse_cell_x!=-1) {
			Color c = get_level_colour(place_mouse_cell_z, 1, 0.5f);
			float col1[] = {c.getRed()/256f, c.getGreen()/256f, c.getBlue()/256f, 0.5f};
			gl.glColor4fv(col1, 0);
			gl.glLoadIdentity();
			gl.glScaled(SCALE_UP, SCALE_UP, 1);
			gl.glTranslated((place_mouse_cell_x+xoff)*SCALE_X, (place_mouse_cell_y+yoff)*SCALE_Y, place_mouse_cell_z);
			tile_model.render(gl);
		}
		
		draw_spikes(gl, SCALE_UP, xoff, yoff, z0, zm);
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		gl.glDisable(GL.GL_LIGHTING);
		float xc = (GameShell.display_region_width/2)-13, yc = 0;
		if (editor_help==null) {
			String help_string = "Left click: Add tile\n" +
					"Right click: Remove tile\n" +
					"\n" +
					"Levels must have 144 tiles.";
			editor_help = new SelfFadingMessage(xc, yc, help_string, font_small);
		}
		editor_help.xpos = xc - editor_help.width/2;
		editor_help.ypos = yc - editor_help.height/2;
		editor_help.render(gl);
		
		yc -= font_small.get_default_line_spacing()*3;
		int tile_count = tile_count();
		float col[] = tile_count==144 ? KSGraphicsConstants.GL_COLOUR_4F_WHITE : KSGraphicsConstants.GL_COLOUR_4F_RED;
		if (tile_count_msg==null) {
			tile_count_msg = new SelfFadingMessage(xc, yc, "Tiles: 144", font_small);
		}
		String help_string = "Tiles: "+tile_count;
		tile_count_msg.setText(help_string, false);
		tile_count_msg.xpos = xc - tile_count_msg.width/2;
		tile_count_msg.ypos = yc - tile_count_msg.height/2;
		tile_count_msg.setThisTextColours(col, col, col);
		tile_count_msg.render(gl);
	}
	
	private void draw_grid(GL gl, final float SCALE_UP, final float xoff, final float yoff, int x0, int y0, int xm, int ym) {
		gl.glLoadIdentity();
		gl.glScaled(SCALE_UP, SCALE_UP, 1);
		gl.glColor4d(1, 1, 1, 0.5);
		gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINES);
		for (int y=0; y<GRID_EXTENT_Y; y++) {
			gl.glVertex2d((x0+xoff)*SCALE_X, (y+yoff)*SCALE_Y);
			gl.glVertex2d((xm+xoff)*SCALE_X, (y+yoff)*SCALE_Y);
		}
		for (int x=0; x<GRID_EXTENT_X; x++) {
			gl.glVertex2d((x+xoff)*SCALE_X, (y0+yoff)*SCALE_Y);
			gl.glVertex2d((x+xoff)*SCALE_X, (ym+yoff)*SCALE_Y);				
		}
		gl.glEnd();
	}
	
	private void draw_spikes(GL gl, final float SCALE_UP, final float xoff, final float yoff, int z0, int zm) {
		gl.glLoadIdentity();
		gl.glScaled(SCALE_UP, SCALE_UP, 1);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glLineWidth(1);
		gl.glBegin(GL.GL_LINES);
		for (int y=0; y<GRID_EXTENT_Y; y++) {
			for (int x=0; x<GRID_EXTENT_X; x++) {
				float b = 1;
				gl.glColor4d(b, b, b, 0.4);
				gl.glVertex3d((x+xoff)*SCALE_X, (y+yoff)*SCALE_Y, z0*SCALE_Z);
				b = 1;
				gl.glColor4d(b, b, b, 0.0);
				gl.glVertex3d((x+xoff)*SCALE_X, (y+yoff)*SCALE_Y, zm*SCALE_Z);
			}
		}
		gl.glEnd();
	}
	
	private int tile_count() {
		boolean cell_matrix[][][] = ((MahjongLayout)the_level).cell_matrix;
		int result = 0;
		for (int z=0; z<GRID_EXTENT_Z; ++z) {
			for (int x=0; x<GRID_EXTENT_X; x++) {
				for (int y=0; y<GRID_EXTENT_Y; y++) {
					if (cell_matrix[x][y][z]) ++result;
				}
			}
		}
		return result;
	}
	
	private boolean can_place_tile_here(int x, int y, int z) {
		if (x>=GRID_EXTENT_X-1 || y>=GRID_EXTENT_Y-1 || x<0 || y<0) return false; // Out of bounds in plane
		boolean cell_matrix[][][] = ((MahjongLayout)the_level).cell_matrix;
		boolean z_ok = (z==0);
		if (!z_ok) {
			int z_test = z-1;
			boolean p1 =	x==0 ||	y==0 ||	cell_matrix[x-1][y-1][z_test],
					p2 = 			y==0 ||	cell_matrix[x  ][y-1][z_test],
					p3 =			y==0 ||	cell_matrix[x+1][y-1][z_test],
					p4 =	x==0 ||			cell_matrix[x-1][y  ][z_test],
					p5 =					cell_matrix[x  ][y  ][z_test],
					p6 =					cell_matrix[x+1][y  ][z_test],
					p7 =	x==0 ||			cell_matrix[x-1][y+1][z_test],
					p8 =					cell_matrix[x  ][y+1][z_test],
					p9 =					cell_matrix[x+1][y+1][z_test];
			boolean q1 = p1 || p2 || p4 || p5;
			boolean q2 = p2 || p3 || p5 || p6;
			boolean q3 = p4 || p5 || p7 || p8;
			boolean q4 = p5 || p6 || p8 || p9;
			z_ok = q1 && q2 && q3 && q4;
		}
		if (!z_ok) {
			return false;
		}
		{ // Very similar to above, but subtly different
			boolean p1 =	x==0 ||	y==0 ||	cell_matrix[x-1][y-1][z],
					p2 = 			y==0 ||	cell_matrix[x  ][y-1][z],
					p3 =			y==0 ||	cell_matrix[x+1][y-1][z],
					p4 =	x==0 ||			cell_matrix[x-1][y  ][z],
					p5 =					cell_matrix[x  ][y  ][z],
					p6 =					cell_matrix[x+1][y  ][z],
					p7 =	x==0 ||			cell_matrix[x-1][y+1][z],
					p8 =					cell_matrix[x  ][y+1][z],
					p9 =					cell_matrix[x+1][y+1][z];
			boolean q1 = p1 || p2 || p4 || p5;
			boolean q2 = p2 || p3 || p5 || p6;
			boolean q3 = p4 || p5 || p7 || p8;
			boolean q4 = p5 || p6 || p8 || p9;
			return !(q1 || q2 || q3 || q4);
		}
	}
	
	public boolean state_mousedown_lmb = false;
	public boolean state_mousedown_rmb = false;
	public void update() {
		super.update();
		
		boolean cell_matrix[][][] = ((MahjongLayout)the_level).cell_matrix;
		
		boolean new_state_mousedown_lmb = (MouseBuffer.buttons_down&1)!=0;
		boolean new_state_mousedown_rmb = (MouseBuffer.buttons_down&4)!=0;
		if (!state_mousedown_lmb && new_state_mousedown_lmb) {
			last_left_click_layer = place_mouse_cell_z;
		}
		else if (state_mousedown_lmb && !new_state_mousedown_lmb) {
			last_left_click_layer = -1;
		}
		if (!state_mousedown_rmb && new_state_mousedown_rmb) {
			last_right_click_layer = remove_mouse_cell_z;
		}
		else if (state_mousedown_rmb && !new_state_mousedown_rmb) {
			last_left_click_layer = -1;
		}
		if (last_left_click_layer==place_mouse_cell_z) {
			if (place_mouse_cell_x!=-1) {
				if ((MouseBuffer.buttons_down&1)!=0) {
					cell_matrix[place_mouse_cell_x][place_mouse_cell_y][place_mouse_cell_z] = true;
					the_level.has_unsaved_changes = true;
				}
			}
		}
		if (last_right_click_layer==remove_mouse_cell_z) {
			if (remove_mouse_cell_x!=-1) {
				if ((MouseBuffer.buttons_down&4)!=0) {
					cell_matrix[remove_mouse_cell_x][remove_mouse_cell_y][remove_mouse_cell_z] = false;
					the_level.has_unsaved_changes = true;
				}
			}
		}
		
		state_mousedown_lmb = new_state_mousedown_lmb;
		state_mousedown_rmb = new_state_mousedown_rmb;
	}
}
