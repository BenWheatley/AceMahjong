package game;

import javax.media.opengl.GL;

import kslib.graphics._3d.AbstractModel3d;
import kslib.graphics._3d.ModelRenderer;

public class OutlineRenderer implements ModelRenderer {
	private static int stencilLayer;
	public void render(AbstractModel3d m, GL gl) {
		if (++stencilLayer>255) stencilLayer = 1;
		gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
		gl.glEnable(GL.GL_STENCIL_TEST);
		gl.glStencilFunc(GL.GL_ALWAYS, stencilLayer, 0xFFFFFFFF);
		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
		m.innerRender(gl);
		gl.glStencilFunc(GL.GL_NOTEQUAL, stencilLayer, 0xFFFFFFFF);
		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE);
		final float B = 0.0f;
		gl.glColor4f(B,B,B,0.5f);
		gl.glLineWidth(2);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		gl.glEnable(GL.GL_POLYGON_OFFSET_LINE);
		gl.glPolygonOffset(-0.16f, 1);
		gl.glDisable(GL.GL_LIGHTING);
		m.innerRender(gl);
		gl.glColor4f(1,1,1,1);
		gl.glPopAttrib();
	}
	
	private static ModelRenderer instance;
	public static ModelRenderer getInstance() {
		if (instance==null) instance = new OutlineRenderer();
		return instance;
	}
}
