package game.menu;

import static game.AceMahjongConstants.*;
import static game.ResourceManager.*;
import static kslib.graphics.KSGraphicsConstants.ALIGN_H_CENTRE;
import static kslib.graphics.KSGraphicsConstants.ALIGN_V_MIDDLE;

import java.io.File;

import game.ResourceManager;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.graphics.KSFont;
import kslib.graphics.KSGraphicsConstants;
import kslib.graphics._3d.*;
import kslib.graphics._3d.procedural.Primatives;
import kslib.io.save_data.Preferences;
import kslib.registration.RegistrationData;
import kslib.ui.Button;
import kslib.ui.SelfFadingMessage;
import kslib.ui.UIComponent;
import kslib.ui.menu.BasicOptionsMenuScreen;
import kslib.ui.menu.menu_options.MenuOption;
import kslib.util.Renderable;

public class SelectLayoutMenuScreen extends BasicOptionsMenuScreen {
	
	private static final float[] COL_SELECTED_TEXT = new float[]{0,0.75f,0,1};
	private static final float[] COL_SELECTED = new float[]{0, 1, 0, 1};
	private static final float[] COL_HOVER = new float[]{0, 1, 1, 1};
	Button buttons[][];
	
	public SelectLayoutMenuScreen(Renderable background_in, MenuOption[] options_in, int default_h_alignment, int default_v_alignment) {
		super(background_in, options_in, default_h_alignment, default_v_alignment);
	}
	
	public void render(GL gl) {
		super.render(gl);
		
		float draw_y = 3*GameShell.display_region_height/8;
		font_title.draw_multiline("Select layout", KSGraphicsConstants.GL_COLOUR_4F_WHITE, 0, draw_y, 0, 0, ALIGN_H_CENTRE, ALIGN_V_MIDDLE, 0, false);
		
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_LIGHT0);
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				
		final float corner_radius = 2;
		
		// 3:5 ratio => width = 7x, height = 3x, area = 3x*7x=21x^2, x = sqrt(A/21)
		final float X = (float)Math.sqrt(layouts.length/21.0);
		final int	GRID_WIDTH = (int)Math.ceil(7*X),
					GRID_HEIGHT = (int)Math.ceil(layouts.length/(float)GRID_WIDTH);
		if (buttons==null) buttons = new Button[GRID_HEIGHT][GRID_WIDTH];
		final float col_off = (GRID_WIDTH-1)/2.0f, row_off = (GRID_HEIGHT-1)/2.0f;
		int i=0;
		final float X_FILL_FRACTION = 0.85f;
		final float Y_FILL_FRACTION = 0.5f;
		final float width = GameShell.display_region_width*X_FILL_FRACTION/GRID_WIDTH,
					height = GameShell.display_region_height*Y_FILL_FRACTION/GRID_HEIGHT;
		for (int row=0; row<GRID_HEIGHT; ++row) {
			for (int col=0; col<GRID_WIDTH && i<layouts.length; ++col) {
				float x_rel = col-col_off;
				float y_rel = 0.2f-(row-row_off);
				float x = width*x_rel;
				float y = height*y_rel;
				float narrow_width = width*0.95f;
				float narrow_height = height*0.95f;
				Button this_button = buttons[row][col];
				KSFont f = font_tiny;
				String label = layouts[i].name;
				if (this_button==null) {
					Model3d button_back = Primatives.rounded_rectangle(narrow_width, narrow_height, corner_radius);
					buttons[row][col] = this_button = new Button(0, 0, button_back, null, null, 1);
					float labelWidth = narrow_width;
					float labelHeight = narrow_height;
					UIComponent labelSc = new UIComponent(0, -narrow_height/2, labelWidth, labelHeight, null, label, f, 1);
					float BLACK[] = KSGraphicsConstants.GL_COLOUR_4F_BLACK;
					labelSc.setThisModelColours(BLACK, BLACK, BLACK);
					labelSc.model = Primatives.rectangle(labelWidth, labelHeight);
					this_button.addSubcomponents(labelSc);
				}
				{
					UIComponent labelSc = this_button.subcomponents.firstElement();
					labelSc.relative_line_spacing = 1;
					labelSc.compactVertical();
					labelSc.ypos = this_button.ypos;
				}
				this_button.moveAllTo(	x - (narrow_width/2),	y - (narrow_height/2)	);
				this_button.clipToRect = true;
				this_button.width = narrow_width;
				this_button.height = narrow_height;
				this_button.enabled = true;
				this_button.setAllTextColourHover(KSGraphicsConstants.GL_COLOUR_4F_PURPLE);
				this_button.mouse_lmb_click_sound = ResourceManager.click;
				layouts[i].prepare_preview(); // Has it's own internal is-not-null shortcut
				Model3d m = (Model3d)this_button.model;
				m.apply_texture(layouts[i].preview, Model3d.TEX_MODE_SHEET_XY, 0.8f);
				
				if (i==selected_layout) {
					this_button.setThisModelColours(COL_SELECTED, COL_SELECTED, COL_SELECTED);
					this_button.setAllTextColourNormal(COL_SELECTED_TEXT);
				}
				else {
					this_button.setThisModelColours(new float[]{1,1,1,1}, COL_HOVER, null);
				}
				
				float C[];
				if (this_button.state_mouseover) C = KSGraphicsConstants.GL_COLOUR_4F_PURPLE;
				else if (i==selected_layout) C = KSGraphicsConstants.GL_COLOUR_4F_GREEN;
				else C = KSGraphicsConstants.GL_COLOUR_4F_WHITE;
				this_button.setAllTextColours(C, C, C);
				
				this_button.render(gl);
				if ( !( RegistrationData.has_registered() || layouts[i].name.compareTo("Dragon")==0 ) ) {
					gl.glLoadIdentity();
					gl.glTranslated(x, y, 0);
					float s = 0.4f;
					gl.glScaled(s, s, s);
					float opacity = 1f;
					gl.glColor4f(1, 1, 1, opacity);
					padlock_billboard.render(gl);
				}
				++i;
			}
		}
		
		gl.glDisable(GL.GL_LIGHTING);
	}
	
	public void update() {
		super.update();
		if (menu_tick<=0) {
			buttons = null;
		}
		if ((menu_tick&31)==0) { // Check, but not every frame
			File layouts_folder = new File(LAYOUTS_PATH);
			if (((ResourceManager)GameShell.resource_manager).layouts_have_changed(layouts_folder)) {
				((ResourceManager)GameShell.resource_manager).loadLayouts(layouts_folder);
				buttons = null;
			}
		}
		if (buttons!=null) {
			final int HEIGHT = buttons.length;
			final int WIDTH = buttons[0].length;
			int i=0;
			for (int row=0; row<HEIGHT; ++row) {
				for (int col=0; col<WIDTH && i<layouts.length; ++col) {
					if (buttons[row][col]!=null) {
						buttons[row][col].update();
						if (buttons[row][col].something_happened) {
							if (buttons[row][col].mouse_lmb_click) {
								if (RegistrationData.has_registered() || layouts[i].name.compareTo("Dragon")==0) {
									selected_layout = i;
									Preferences.set(KEY_SELECTED_LAYOUT, ResourceManager.layouts[selected_layout].hash);
								}
								else {
									SelfFadingMessage sfm = new SelfFadingMessage(0, 0, get_layout_not_free_message(i), font_not_free);
									float c[] = new float[]{0,0,0,0.75f};
									sfm.setThisModelColours(c, c, c);
									GameShell.messages.add(sfm);
								}
							}
						}
						++i;
					}
				}
			}
		}
	}
	
	private String get_layout_not_free_message(int layout_index) {
		return FREE_PLAYERS_ONLY_GET_DRAGON.replaceAll("\\$1", "\""+layouts[layout_index].name+"\"");
	}
}
