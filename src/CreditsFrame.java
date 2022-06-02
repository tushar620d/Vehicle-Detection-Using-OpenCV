import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class CreditsFrame extends JFrame {


	private static final long serialVersionUID = 1L;
	
	public CreditsFrame(String frameName) {
		init(frameName);
	}

	public void init(String frameName) {
		this.setSize(400,300);
		this.setTitle(frameName);
		this.setResizable(false);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.getContentPane().setBackground(new Color(12, 12, 12));
		this.setLayout(null);
		setTitle();
		setPresenteBy();
		setDeveloperName();
		setGuidedBy();
		setGuid();
		setGuidInfo();
	}
	
	public void setTitle() {
		JLabel label = new JLabel("Real time vehicle recognition",JLabel.CENTER);
		label.setText("Real Time Vehicle Detection");
		label.setFont(new Font("Tahoma",Font.BOLD,20));
		label.setForeground(new Color(228,230,235));
		label.setBounds(0,0,this.getWidth(),25);
		this.add(label);
	}
	public void setPresenteBy() {
		JLabel label = new JLabel("Presented By:",JLabel.CENTER);
		label.setFont(new Font(label.getFont().getFamily(),Font.BOLD,15));
		label.setForeground(new Color(228,230,235));
		label.setBounds(0,30,this.getWidth(),25);
		this.add(label);
	}
	public void setDeveloperName() {
		JLabel label = new JLabel("<html><div style='text-align:center'>Vaibhav Upadhyay"
				+ "<br>Vaibhav Guddhade" + 
				"<br>Varun Siriah"
				+ "<br>Minal Dongre</div></html>",JLabel.CENTER);
		label.setFont(new Font(label.getFont().getFamily(),Font.BOLD,17));
		label.setForeground(new Color(228,230,235));
		label.setBounds(0,50,this.getWidth(),100);
		this.add(label);
	}
	public void setGuidedBy() {
		JLabel label = new JLabel("Guided By:",JLabel.CENTER);
		label.setFont(new Font(label.getFont().getFamily(),Font.BOLD,15));
		label.setForeground(new Color(228,230,235));
		label.setBounds(0,160,this.getWidth(),25);
		this.add(label);
	}
	public void setGuid() {
		JLabel label = new JLabel("Mr.  Siddhant jaiswal",JLabel.CENTER);
		label.setFont(new Font(label.getFont().getFamily(),Font.BOLD,17));
		label.setForeground(new Color(228,230,235));
		label.setBounds(0,150,this.getWidth(),100);
		this.add(label);
	}
	public void setGuidInfo() {
		JLabel label = new JLabel("<html><div style='text-align:center'>Asst. Professor, Computer Science and"
				+ "<br>Engineering JIT, Nagpur</div></html>",JLabel.CENTER);
		label.setFont(new Font(label.getFont().getFamily(),Font.BOLD,13));
		label.setForeground(new Color(228,230,235));
		label.setBounds(0,180,this.getWidth(),100);
		this.add(label);
	}
	

}
