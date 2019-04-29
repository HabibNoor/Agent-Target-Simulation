import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class Visualizer extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Visualizer(Grid grid) {
		super("AI Project");

		Board a = new Board(grid);
		setSize(1450, 1000);
		setLocationRelativeTo(null);
		add(a, BorderLayout.CENTER);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		setVisible(true);
		toFront();
		

	}

	public class Board extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Grid grid;

		public Board(Grid grid) {
			this.grid = grid;
		}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);

			int target_size = 6;
			int agent_size = 8;
			String tempPlace = "  ";
			String tempPlace1 = "  ";
			String tempPlace2 = "  ";
			String tempPlace3 = "  ";
			String tempPlace4 = "  ";

			g.drawRect(100, 100, 800, 800);
			g.setFont(new Font("Trajan", Font.BOLD, 32));
			g.drawString(grid.gameType + " Search", 400, 50);
			g.setFont(new Font("Trajan", Font.BOLD, 14));
			g.drawString("By: Jonathan Fischer, Habibullah Noorzaie & Viraj Patel", 1025, 950);

			if (grid.agent[0].finishPlace != -1) {
				tempPlace = Integer.toString(grid.agent[0].finishPlace);
			}
			if (grid.agent[1].finishPlace != -1) {
				tempPlace1 = Integer.toString(grid.agent[1].finishPlace);
			}
			if (grid.agent[2].finishPlace != -1) {
				tempPlace2 = Integer.toString(grid.agent[2].finishPlace);
			}
			if (grid.agent[3].finishPlace != -1) {
				tempPlace3 = Integer.toString(grid.agent[3].finishPlace);
			}
			if (grid.agent[4].finishPlace != -1) {
				tempPlace4 = Integer.toString(grid.agent[4].finishPlace);
			}

			
			DecimalFormat df = new DecimalFormat("#.000000");
			
			g.setFont(new Font("Cambria", Font.BOLD, 16));
			g.drawRect(950, 100, 410, 280);
			g.drawString(" Agents:          " + "Score   " + " Position       Steps         Happiness", 950, 85);
			g.setColor(new Color(255, 165, 0));
			g.drawString(" Agent 0:", 950, 150);
			g.setColor(Color.black);
			g.drawString(grid.agent[0].collectedCount + " / 5  " + "            " + tempPlace + "           " + grid.agent[0].steps + "                " + df.format(grid.getHappiness(0)), 1050, 150);
			g.setColor(Color.red);
			g.drawString(" Agent 1:", 950, 200);
			g.setColor(Color.black);
			g.drawString(grid.agent[1].collectedCount + " / 5  " + "            " + tempPlace1 + "           " + grid.agent[1].steps + "                " + df.format(grid.getHappiness(1)), 1050, 200);
			g.setColor(new Color(50, 205, 50));
			g.drawString(" Agent 2:", 950, 250);
			g.setColor(Color.black);
			g.drawString(grid.agent[2].collectedCount + " / 5   " + "           " + tempPlace2 + "           " + grid.agent[2].steps + "                " + df.format(grid.getHappiness(2)), 1050, 250);
			g.setColor(new Color(0, 191, 255));
			g.drawString(" Agent 3:", 950, 300);
			g.setColor(Color.black);
			g.drawString(grid.agent[3].collectedCount + " / 5   " + "           " + tempPlace3 + "           " + grid.agent[3].steps + "                " + df.format(grid.getHappiness(3)), 1050, 300);
			g.setColor(new Color(186, 85, 211));
			g.drawString(" Agent 4:", 950, 350);
			g.setColor(Color.black);
			g.drawString(grid.agent[4].collectedCount + " / 5   " + "           " + tempPlace4 + "           " + grid.agent[4].steps + "                " + df.format(grid.getHappiness(4)), 1050, 350);

			g.setColor(new Color(211, 211, 211));
			for (int x = 100; x < 908; x += 8) {
				g.drawLine(x, 100, x, 900);
			}

			for (int y = 100; y < 908; y += 8) {
				g.drawLine(100, y, 900, y);
			}

			g.setColor(Color.black);
			g.fillOval(grid.agent[0].x * 8 + 100 - 4, (100 - grid.agent[0].y) * 8 + 100 - 4, agent_size + 1,
					agent_size + 1);
			g.setColor(new Color(255, 165, 0));
			g.drawOval((grid.agent[0].x * 8 + 100 - 4) - 75, ((100 - grid.agent[0].y) * 8 + 100 - 4) - 75, 160, 160);
			g.fillOval(grid.agent[0].x * 8 + 100 - 4, (100 - grid.agent[0].y) * 8 + 100 - 4, agent_size, agent_size);
			for (int i = 0; i < 5; i++) {
				if (grid.agent[0].target[i].collected == false) {
					g.setColor(Color.black);
					g.fillRect(grid.agent[0].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[0].target[i].y) * 8 + 100 - (target_size / 2), target_size + 1,
							target_size + 1);
					g.setColor(new Color(255, 165, 0));
					g.fillRect(grid.agent[0].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[0].target[i].y) * 8 + 100 - (target_size / 2), target_size, target_size);
				}
			}
			g.setColor(Color.black);
			g.fillOval(grid.agent[1].x * 8 + 100 - 4, (100 - grid.agent[1].y) * 8 + 100 - 4, agent_size + 1,
					agent_size + 1);
			g.setColor(Color.red);
			g.drawOval((grid.agent[1].x * 8 + 100 - 4) - 75, ((100 - grid.agent[1].y) * 8 + 100 - 4) - 75, 160, 160);
			g.fillOval(grid.agent[1].x * 8 + 100 - 4, (100 - grid.agent[1].y) * 8 + 100 - 4, agent_size, agent_size);
			for (int i = 0; i < 5; i++) {
				if (grid.agent[1].target[i].collected == false) {
					g.setColor(Color.black);
					g.fillRect(grid.agent[1].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[1].target[i].y) * 8 + 100 - (target_size / 2), target_size + 1,
							target_size + 1);
					g.setColor(Color.red);
					g.fillRect(grid.agent[1].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[1].target[i].y) * 8 + 100 - (target_size / 2), target_size, target_size);
				}
			}
			g.setColor(Color.black);
			g.fillOval(grid.agent[2].x * 8 + 100 - 4, (100 - grid.agent[2].y) * 8 + 100 - 4, agent_size + 1,
					agent_size + 1);
			g.setColor(new Color(50, 205, 50));
			g.drawOval((grid.agent[2].x * 8 + 100 - 4) - 75, ((100 - grid.agent[2].y) * 8 + 100 - 4) - 75, 160, 160);
			g.fillOval(grid.agent[2].x * 8 + 100 - 4, (100 - grid.agent[2].y) * 8 + 100 - 4, agent_size, agent_size);
			for (int i = 0; i < 5; i++) {
				if (grid.agent[2].target[i].collected == false) {
					g.setColor(Color.black);
					g.fillRect(grid.agent[2].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[2].target[i].y) * 8 + 100 - (target_size / 2), target_size + 1,
							target_size + 1);
					g.setColor(new Color(50, 205, 50));
					g.fillRect(grid.agent[2].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[2].target[i].y) * 8 + 100 - (target_size / 2), target_size, target_size);
				}
			}
			g.setColor(Color.black);
			g.fillOval(grid.agent[3].x * 8 + 100 - 4, (100 - grid.agent[3].y) * 8 + 100 - 4, agent_size + 1,
					agent_size + 1);
			g.setColor(new Color(0, 191, 255));
			g.drawOval((grid.agent[3].x * 8 + 100 - 4) - 75, ((100 - grid.agent[3].y) * 8 + 100 - 4) - 75, 160, 160);
			g.fillOval(grid.agent[3].x * 8 + 100 - 4, (100 - grid.agent[3].y) * 8 + 100 - 4, agent_size, agent_size);
			for (int i = 0; i < 5; i++) {
				if (grid.agent[3].target[i].collected == false) {
					g.setColor(Color.black);
					g.fillRect(grid.agent[3].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[3].target[i].y) * 8 + 100 - (target_size / 2), target_size + 1,
							target_size + 1);
					g.setColor(new Color(0, 191, 255));
					g.fillRect(grid.agent[3].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[3].target[i].y) * 8 + 100 - (target_size / 2), target_size, target_size);
				}
			}
			g.setColor(Color.black);
			g.fillOval(grid.agent[4].x * 8 + 100 - 4, (100 - grid.agent[4].y) * 8 + 100 - 4, agent_size + 1,
					agent_size + 1);
			g.setColor(new Color(186, 85, 211));
			g.drawOval((grid.agent[4].x * 8 + 100 - 4) - 75, ((100 - grid.agent[4].y) * 8 + 100 - 4) - 75, 160, 160);
			g.fillOval(grid.agent[4].x * 8 + 100 - 4, (100 - grid.agent[4].y) * 8 + 100 - 4, agent_size, agent_size);
			for (int i = 0; i < 5; i++) {
				if (grid.agent[4].target[i].collected == false) {
					g.setColor(Color.black);
					g.fillRect(grid.agent[4].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[4].target[i].y) * 8 + 100 - (target_size / 2), target_size + 1,
							target_size + 1);
					g.setColor(new Color(186, 85, 211));
					g.fillRect(grid.agent[4].target[i].x * 8 + 100 - (target_size / 2),
							(100 - grid.agent[4].target[i].y) * 8 + 100 - (target_size / 2), target_size, target_size);
				}
			}

		}
	}
}
