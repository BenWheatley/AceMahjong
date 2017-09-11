package game;

import static kslib.globals.MiscGlobals.DEBUG;

import java.awt.event.KeyEvent;

import javax.media.opengl.GL;

import kslib.GameShell;
import kslib.CheatModuleClass.DefaultCheatModule;
import kslib.io.KeyboardBuffer;

public class CheatModule extends DefaultCheatModule {
	public void update(GL gl) {
		if (DEBUG) {
			super.update();
			if (GameShell.game!=null) {
				if (KeyboardBuffer.keyHeld[KeyEvent.VK_F5]) ((AceMahjongGame) GameShell.game).win();
				if (KeyboardBuffer.keyHeld[KeyEvent.VK_F6]) ((AceMahjongGame) GameShell.game).lose();
			}
		}
	}
}
