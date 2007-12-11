/** Basic template for a simple jaav gui program */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.lindenb.lang.RunnableObject;
import org.lindenb.swing.SwingUtils;
import org.lindenb.util.Debug;


class Model
	{
	
	}

/**
 * XInternalFrame
 * @author pierre
 *
 */
abstract class XInternalFrame extends JInternalFrame
	{
	protected XInternalFrame(String title)
		{
		super(title,true,false,true,true);
		this.addInternalFrameListener(new InternalFrameAdapter()
			{
			@Override
			public void internalFrameOpened(InternalFrameEvent e) {
				Dimension d= getGUI1().getDesktopPane().getSize();
				Random rand= new Random(System.currentTimeMillis());
				setBounds(rand.nextInt(50), rand.nextInt(50), d.width-100, d.height-100);
				XInternalFrame.this.removeInternalFrameListener(this);
				}
			});
		}
	public abstract GUI1 getGUI1();
	public Model getModel() { return getGUI1().getModel();}
	}

/**
 * @author pierre
 *
 */
public class GUI1 extends JFrame
	{
	private static final long serialVersionUID = 1L;
	private JDesktopPane desktopPane;
	private Model model;
	
	/** GUI1 */
	private GUI1(Model model)
		{
		super("GUI1");
		this.model=model;
		SwingUtils.center(this, 50);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JPanel contentPane= new JPanel(new BorderLayout(2,2));
		setContentPane(contentPane);
		contentPane.setBorder(new EmptyBorder(2,2,2,2));
		this.desktopPane= new JDesktopPane();
		contentPane.add(this.desktopPane,BorderLayout.CENTER);
		
		this.addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowOpened(WindowEvent e) {
				XInternalFrame f= new XInternalFrame("")
					{
					private static final long serialVersionUID = 1L;

					@Override
					public GUI1 getGUI1() {
						return GUI1.this;
						}
					};
				e.getWindow().removeWindowListener(this);
				desktopPane.add(f);
				f.setVisible(true);
				}
			
			});
		
		this.addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowClosing(WindowEvent e)
				{
				doMenuClosing();
				}
			});
		}
	
	public void doMenuClosing()
		{
		this.setVisible(false);
		this.dispose();
		}
	
	public JDesktopPane getDesktopPane()
		{
		return this.desktopPane;
		}
	
	public Model getModel()
		{
		return this.model;
		}
	
	
	public static void main(String[] args)
		{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		Debug.setDebugging(false);
		
		Debug.setDebugging(true);
		try
			{
			Model m =new Model();
			SwingUtilities.invokeAndWait(new RunnableObject<Model>(m)
				{
				@Override
				public void run()
					{
					JFrame f= new GUI1(getObject());
					f.setVisible(true);
					}
				});
			} catch(Exception err)
			{
			err.printStackTrace();
			}
		}

}
