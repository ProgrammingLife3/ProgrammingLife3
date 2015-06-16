package tudelft.ti2806.pl3.ui.util;

import javax.swing.JOptionPane;

/**
 * Created by Boris Mattijssen on 14-06-15.
 */
public class DialogUtil {

	private DialogUtil() {
	}

	/**
	 * Open a confirm dialog.
	 *
	 * @param title
	 * 		the title
	 * @param message
	 * 		the message to display
	 * @return the dialog
	 */
	public static boolean confirm(String title, String message) {
		int answer = JOptionPane
				.showConfirmDialog(null, message, title,
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
		return answer == JOptionPane.YES_OPTION;
	}
}
