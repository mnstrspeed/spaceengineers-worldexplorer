import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SpacePanel extends JPanel implements MouseWheelListener, KeyListener
{
	private JFrame frame;
	
	private List<Entity> entities;
	private Entity player;
	private double scale;
	private double rotation;
	private boolean showLabels;
	
	public SpacePanel(JFrame frame, List<Entity> entities, Entity player)
	{
		super();
		this.frame = frame;
		this.entities = entities;
		this.player = player;
		
		this.showLabels = true;
		this.rotation = 0.0;
		
		this.decideScale();
		
		this.setFocusable(true);
		this.requestFocusInWindow();
		this.addMouseWheelListener(this);
		this.addKeyListener(this);
	}

	public void decideScale() {
		this.scale = 0;
		for (Entity entity : entities)
		{
			if (entity.x > this.scale)
				this.scale = entity.x;
			if (entity.y > this.scale)
				this.scale = entity.y;
			if (entity.y > this.scale)
				this.scale = entity.z;
		}
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
				RenderingHints.VALUE_RENDER_QUALITY);
		
		int maxBlockCount = this.entities.get(0).blockCount;
		for (Entity entity : this.entities)
			if (entity.blockCount > maxBlockCount)
				maxBlockCount = entity.blockCount;
		
		double[] transform = rotY(new double[16], this.rotation);
		
		// Clear background
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, (int)this.getSize().getWidth(), (int)this.getSize().getHeight());
		
		// Draw entities
		for (Entity entity : this.entities)
		{
			double drawSize = 5 + (entity.blockCount * 15) / maxBlockCount;
			drawEntity(g, transform, drawSize, entity, Color.RED);
		}
		// Draw player
		drawEntity(g, transform, 3, player, Color.YELLOW);

		// Draw scale and rotation text
		g.setColor(Color.WHITE);
		g.drawString(String.format("SCALE: %,.0f meter (use scroll wheel to zoom)", this.scale), 10, 20);
		g.drawString(String.format("ROTATION: %.2f degrees (press arrow keys to rotate)", Math.toDegrees(this.rotation)), 10, 40);
		g.drawString(String.format("Press space to %s labels", showLabels ? "hide" : "show"), 10, 60);
	}

	public void drawEntity(Graphics g, double[] transform, double size, 
			Entity entity, Color color) 
	{
		Point p = mapEntityToPoint(transform, entity);
		
		double offsetX = 0, offsetY = 0, w = 0;
		if (this.getSize().getWidth() > this.getSize().getHeight())
		{
			offsetX = (this.getWidth() - this.getHeight()) / 2;
			w = this.getSize().getHeight() / 2.0;
		}
		else
		{
			offsetY = (this.getHeight() - this.getWidth()) / 2;
			w = this.getSize().getWidth() / 2.0;
		}
		
		g.setColor(color);
		g.drawOval(
				(int)(offsetX + w + (p.x * w) / scale), 
				(int)(offsetY + w + (p.y * w) / scale), 
				(int)size, (int)size);
		
		if (showLabels)
		{
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("(" + (int)entity.x + "," + (int)entity.y + "," + (int)entity.z + ")",
					(int)(offsetX + w + (p.x * w) / scale),
					(int)(offsetY + w + (p.y * w) / scale));
		}
	}
	
	public static Point mapEntityToPoint(double[] transform, Entity entity)
	{
		double x = transform[0] * entity.x + transform[1] * entity.y + transform[2] * entity.z;
		double y = transform[4] * entity.x + transform[5] * entity.y + transform[6] * entity.z;
		
		return new Point((int)x, (int)y);
	}
	
	public static double[] rotX(double[] mat, double angle)
	{
		// Borrowed from Transform3D
		double sinAngle = Math.sin(angle);
        double cosAngle = Math.cos(angle);

        mat[0] = 1.0;	mat[1] = 0.0;		mat[2] = 0.0;		mat[3] = 0.0;
        mat[4] = 0.0;	mat[5] = cosAngle;	mat[6] = -sinAngle;	mat[7] = 0.0;
        mat[8] = 0.0;	mat[9] = sinAngle;	mat[10] = cosAngle;	mat[11] = 0.0;
        mat[12] = 0.0;	mat[13] = 0.0;		mat[14] = 0.0;		mat[15] = 1.0;
        
        return mat;
	}
	
	public static double[] rotY(double[] mat, double angle)
	{
		// Borrowed from Transform3D
        double sinAngle = Math.sin(angle);
        double cosAngle = Math.cos(angle);

        mat[0] = cosAngle;	mat[1] = 0.0;	mat[2] = sinAngle;	mat[3] = 0.0;
        mat[4] = 0.0;		mat[5] = 1.0;	mat[6] = 0.0;		mat[7] = 0.0;
        mat[8] = -sinAngle;	mat[9] = 0.0;	mat[10] = cosAngle;	mat[11] = 0.0;
        mat[12] = 0.0;		mat[13] = 0.0;	mat[14] = 0.0;		mat[15] = 1.0;
        
        return mat;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		scale *= e.getWheelRotation() < 0 ? 0.9 : (1.0 / 0.9);
		frame.repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			showLabels = !showLabels;
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			if (e.getKeyCode() == KeyEvent.VK_LEFT)
				rotation -= 0.05;
			else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
				rotation += 0.05;
			
			if (rotation < 0)
				rotation += 2 * Math.PI;
			if (rotation > 2 * Math.PI)
				rotation -= 2 * Math.PI;
		}
		
		frame.repaint();
	}
	
	@Override public void keyReleased(KeyEvent e) { }
	@Override public void keyTyped(KeyEvent e) { }
}
