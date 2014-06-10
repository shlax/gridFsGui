package org.gfs.gui

import javax.swing.{SwingUtilities, WindowConstants, JDialog}

class UploadDialog extends JDialog(Gui()){
  assert(SwingUtilities.isEventDispatchThread)

  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  setModal(true)


}
