package mining.UserInterface;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import mining.Database.GraphDatabase;
import mining.EntityLinkingControl.EntityLinking;
import mining.EntityLinkingControl.ExtractEntity;
import mining.EntityLinkingStructure.CandidateStore;
import mining.EntityLinkingStructure.EntityStore;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.InvalidFormatException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.TextArea;
import java.awt.TextField;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;


import mining.Database.*;
import javax.swing.JTextPane;

public class NameEntityLinkingUI extends JFrame  {

	private JPanel contentPane;
	private JTextArea txtareaInput;
	private JButton btnInput;
	private ArrayList<ArrayList<String[]>> nameEntities;
	private JLabel lbtarget;
	private ArrayList<Parse> entities;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NameEntityLinkingUI frame = new NameEntityLinkingUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public NameEntityLinkingUI() throws InvalidFormatException, IOException {
		super("Named Entity Linking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(1350, 800);
        getContentPane().setLayout(null);
        
        JLabel lbInput = new JLabel("Please enter paragraph or sentences: ");
        lbInput.setVerticalAlignment(SwingConstants.TOP);
        lbInput.setBounds(20, 20, 400, 40);
        getContentPane().add(lbInput);
        
        JLabel lbtargetHead = new JLabel("Target entities: ");
        lbtargetHead.setVerticalAlignment(SwingConstants.TOP);
        lbtargetHead.setBounds(20, 369, 300, 23);
        getContentPane().add(lbtargetHead);
        
        JTextArea txtTarget = new JTextArea();
        txtTarget.setLineWrap(true);
        txtTarget.setEditable(false);
        txtTarget.setText("None");
        txtTarget.setBounds(20, 394, 400, 334);
        getContentPane().add(txtTarget);
        
        //txtareaInput = new JTextArea("McNealy finished, he was pretty much squarely in Sun's camp. Scott explains what open means...", 6, 10);
        //txtareaInput = new JTextArea("Bulls should still aim for a title, eventhrough the horrible news. Tyson Chandler says Tony Allen is the best on-ball defender in the #NBA", 6, 10);
        txtareaInput = new JTextArea("McNealy finished, he was pretty much squarely in Sun's camp", 6, 10);
        txtareaInput.setLineWrap ( true );
        txtareaInput.setWrapStyleWord(true);
        txtareaInput.setBounds(20, 46, 828, 250);
        getContentPane().add(txtareaInput);
        
        btnInput = new JButton("Try Linking");
        btnInput.setBounds(344, 312, 150, 23);
        getContentPane().add(btnInput);
        
        JLabel lbCandidateHead = new JLabel("Candidate entities:");
        lbCandidateHead.setBounds(448, 369, 177, 20);
        getContentPane().add(lbCandidateHead);
        
        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(448, 393, 400, 335);

        getContentPane().add(scrollPane_1);
        
        JTextArea txtCandidate = new JTextArea();
        scrollPane_1.setViewportView(txtCandidate);
        scrollPane_1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        txtCandidate.setLineWrap(true);
        txtCandidate.setEditable(false);
        txtCandidate.setText("None");
        
        JLabel lblNewLabel = new JLabel("New label");
        lblNewLabel.setBounds(884, 20, 69, 20);
        getContentPane().add(lblNewLabel);

        
        btnInput.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
        		try {
					EntityLinking entityLinking = new EntityLinking(txtareaInput.getText());
					txtTarget.setText(showTargetEntities(entityLinking));
					txtCandidate.setText(showCandidateEntities(entityLinking));
				} catch (IOException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        	}
        });
        

	}
	
	public String showTargetEntities(EntityLinking entityLinking){
		String targetString = "";
		for (EntityStore entity: entityLinking.getEntityStoreArr()) {
			targetString += entity.getEntityMention() + "\n";
		}
		return targetString;
	}
	
	public String showCandidateEntities(EntityLinking entityLinking){
		String candidateString = "";
		for (EntityStore entity: entityLinking.getEntityStoreArr()) {
			candidateString += "-----" + entity.getEntityMention() + "-----\n";
			for (CandidateStore candidate: entity.getCandidateEntities()){
				candidateString += replaceExtraChar(candidate.getCandidateName()) + ", ";
			}
			candidateString += "\n";
		}
		return candidateString;
	}
	

	public String replaceExtraChar(String input){
		input = input.replace("%28", "(");
		input = input.replace("%29", ")");
		input = input.replace("_", " ");
		input = input.replace("%2C", ",");
		input = input.replace("'s", "");
		input = input.replaceAll("[\\.@#\\%]", " ");
		input = input.replace("_", " ");
		return input;
	}

}
