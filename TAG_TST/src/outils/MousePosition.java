package outils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class MousePosition extends JPanel
{
    private static final int POLL_TIME = 200;
    private static final Dimension SIZE = new Dimension(100, 30);
    private JLabel displayLabel;
    private JPopupMenu popup;
    private Timer timer = new Timer(POLL_TIME, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            PointerInfo info = MouseInfo.getPointerInfo();
            updateDisplay(info.getLocation());
        }
    });
    
    public MousePosition() {
        super(new BorderLayout());
        setBorder(new LineBorder(Color.BLACK, 2));
        createPopUp();
        displayLabel = new JLabel();
        displayLabel.setHorizontalAlignment(JLabel.CENTER);
        displayLabel.setForeground(Color.WHITE);
        add(displayLabel);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
            	System.out.println("on clique !");
                maybeShowPopup(e);
                System.exit(0);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    popup.show(e.getComponent(),
                        e.getX(), e.getY());
                }
            }
        });
        timer.start();
    }

    private void updateDisplay(Point p)
    {
        displayLabel.setText(p.x + " , " + p.y);
    }

    @Override
    public Dimension getPreferredSize()
    {
        return SIZE;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        GradientPaint paint = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), Color.BLACK);
        ((Graphics2D) g).setPaint(paint);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void createPopUp()
    {
        popup = new JPopupMenu();
        popup.add(new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
    }

    private static void showGUI()
    {
        JDialog container = new JDialog();
        //container.setUndecorated(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        container.setAlwaysOnTop(true);
        container.setContentPane(new MousePosition());
        container.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        container.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e)
            {
                System.exit(0);
            }
        });
        container.pack();
        //container.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width - container.getPreferredSize().width - 10, 40);
        //container.setSize(screenSize);
        container.setVisible(true);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                showGUI();
            }
        });
    }
}