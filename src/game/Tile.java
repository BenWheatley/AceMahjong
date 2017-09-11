package game;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.graphics._3d.Model3d;
import kslib.graphics._3d.util.ShadowUtil;
import kslib.ui.Button;

public class Tile extends Button {
	boolean highlighted;
	boolean hinted;
	private int id;
	byte opacity = (byte)0x7F;
	private Model3d shadow_model;
	
	public Tile(int id_in) {
		super(0, 0, ResourceManager.tile_models[id_in], null, null, 0);
		set_id(id_in);
		float b = .8f;
		model_colour_disabled = new float[]{b, b, b, 1};
		enabled = false;
	}
	
	public boolean compare_id(Tile other) {	return id==other.id;	}
	public int get_id() {	return id;	}
	public void set_id(int new_id) {
		id = new_id;
		model = ResourceManager.tile_models[new_id];
	}
	
	public boolean fade_out() {
		if (opacity==(byte)0x7F) return false;
		opacity-=5;
		return opacity<0;
	}
	
	public void render(GL gl, float[] col) {
		positionModel(gl);
		if (enabled) calculateCollideWithMouse(gl);
		
		gl.glEnable(GL.GL_BLEND);
		if (collideWithMouse() && enabled) gl.glColor4b((byte)0, (byte)0, (byte)0x7F, opacity);
		else if (highlighted) gl.glColor4b((byte)0x7F, (byte)0, (byte)0, opacity);
		else if (hinted) {
			double slow_tick = GameShell.logic_tick/10.0;
			gl.glColor4b(	(byte)(col[0]*0x7F*(Math.sin(slow_tick)+1)/2),
							(byte)(col[1]*0x7F*(Math.sin(2*Math.PI/3+slow_tick)+1)/2),
							(byte)(col[2]*0x7F*(Math.sin(-2*Math.PI/3+slow_tick)+1)/2),
							(byte)(col[3]*opacity));
		}
		else gl.glColor4b((byte)(col[0]*0x7F), (byte)(col[1]*0x7F), (byte)(col[2]*0x7F), (byte)(col[3]*opacity));
		double z = opacity/(double)(0x7F);
		gl.glPushMatrix();
		gl.glScaled(z, z, z);
		model.render(gl);
		gl.glPopMatrix();
	}
	
	public void render_shadow(GL gl) {
		positionModel(gl);
		if (shadow_model==null) {
			float modelview_matrix[] = new float[16];
			gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, modelview_matrix, 0);
			float light[] = new float[3];
			gl.glGetLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light, 0);
			shadow_model = ShadowUtil.calculate_shadow_volume((Model3d)model, modelview_matrix, light[0], light[1], light[2]);
		}
		shadow_model.render(gl);
	}
}
