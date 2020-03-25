package components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;

public class AddressBook extends JPanel implements ActionListener, FocusListener, ListSelectionListener 
{

		
	JTextField nameField, streetField, cityField, searchField;
	JFormattedTextField zipField, phoneField;
	JComboBox<String> stateBox;
    Font regularFont, italicFont;
    DefaultListModel listModel;
    JLabel addressDisplay;
    JList<String> results;
    String[][] fileContents;
    JButton update, delete, add;
    JScrollPane listScrollPane;
    int[] resultRow;
    int maxRow, selectedRow;
    final static int GAP = 10;
    
    
    public static void main(String[] args)
	{
		//Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run() 
            {
                //Turn off metal's use of bold fonts
            	UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
	}
    
    public AddressBook() 
    {
    	setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    	resultRow = new int[100];

        JPanel leftHalf = new JPanel() 
        {
            //Don't allow us to stretch vertically.
            public Dimension getMaximumSize()
            {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE,
                                     pref.height);
            }
        };
        leftHalf.setLayout(new BoxLayout(leftHalf,
                                         BoxLayout.PAGE_AXIS));
        JPanel panel = new JPanel(new SpringLayout());
        
        //Add text input fields
        nameField = new JTextField();
        nameField.setColumns(20);
        panel.add(new JLabel("Name:",JLabel.TRAILING));
        panel.add(nameField);
        nameField.addActionListener(this);
        nameField.addFocusListener(this);
        
        streetField = new JTextField();
        streetField.setColumns(20);
        panel.add(new JLabel("Street Address:",JLabel.TRAILING));
        panel.add(streetField);
        streetField.addActionListener(this);
        streetField.addFocusListener(this);
        
        cityField = new JTextField();
        cityField.setColumns(20);
        panel.add(new JLabel("City:",JLabel.TRAILING));
        panel.add(cityField);
        cityField.addActionListener(this);
        cityField.addFocusListener(this);
        
        String[] stateStrings = getStateStrings();
        stateBox = new JComboBox<>(stateStrings);
        panel.add(new JLabel("State:" , JLabel.TRAILING));
        panel.add(stateBox);
        stateBox.addActionListener(this);
        stateBox.addFocusListener(this);
        
        zipField = new JFormattedTextField(createFormatter("#####"));
        panel.add(new JLabel("Zip Code:",JLabel.TRAILING));
        panel.add(zipField);
        zipField.addActionListener(this);
        zipField.addFocusListener(this);
        
        phoneField = new JFormattedTextField(createFormatter("##########"));
        panel.add(new JLabel("Phone Number(no dashes):",JLabel.TRAILING));
        panel.add(phoneField);
        phoneField.addActionListener(this);
        phoneField.addFocusListener(this);
        
        SpringUtilities.makeCompactGrid(panel,
                6, 2,
                GAP, GAP, //init x,y
                GAP, GAP/2);//xpad, ypad
        leftHalf.add(panel);
        
        //add buttons
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        
        
        add = new JButton("Add to Spreadsheet");
        add.addActionListener(this);
        add.setActionCommand("add");
        panel2.add(add);
        
        update = new JButton("Update");
        update.addActionListener(this);
        update.setActionCommand("update");
        panel2.add(update);
        update.setVisible(false);
        
        delete = new JButton("Delete Entry");
        delete.addActionListener(this);
        delete.setActionCommand("delete");
        panel2.add(delete);
        delete.setVisible(false);
        
        panel2.setBorder(BorderFactory.createEmptyBorder(0, 0,
                GAP-5, GAP-5));
        
        leftHalf.add(panel2);
        

        add(leftHalf);
        JPanel rightHalf = new JPanel(new BorderLayout());
        JPanel panel3 = new JPanel( new SpringLayout());
       
        /* build Search list results */
        listModel = new DefaultListModel();
        
        //Create the list and put it in a scroll pane.
        results = new JList(listModel);
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.setSelectedIndex(0);
        results.addListSelectionListener(this);
        
        results.setVisibleRowCount(5);
        listScrollPane = new JScrollPane(results);
       
        
        searchField = new JTextField();
        searchField.setColumns(20);
        panel3.add(new JLabel("Search",JLabel.TRAILING));
        panel3.add(searchField);
        searchField.addActionListener(this);
        searchField.addFocusListener(this);
        JButton search = new JButton("Search");
        search.addActionListener(this);
        search.setActionCommand("search");
        panel3.add(search);
     
       panel3.add(listScrollPane);
       SpringUtilities.makeCompactGrid(panel3,
               4,1,
               GAP, GAP, //init x,y
               GAP, GAP/2);//xpad, ypad      
         rightHalf.setBorder(BorderFactory.createEmptyBorder(
                GAP, //top
                0,     //left
                GAP, //bottom
                0));   //right 
        
        rightHalf.add(new JSeparator(JSeparator.VERTICAL),
                BorderLayout.LINE_START);
      
      	 rightHalf.setPreferredSize(new Dimension(200, 150));
       
         rightHalf.add(new JSeparator(JSeparator.HORIZONTAL),
                 BorderLayout.LINE_START);

        rightHalf.add(panel3);
        add(rightHalf);
        fileContents = new String[100][6];
        readFile();   // get all data from file   
         
    }
    
    protected MaskFormatter createFormatter(String s)
    {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (java.text.ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
            System.exit(-1);
        }
        return formatter;
    }

	@Override
	public void focusGained(FocusEvent e) 
	{

	}


	@Override
	public void focusLost(FocusEvent e) {}
	
	public String[] getStateStrings() {
        String[] stateStrings = {
            "Alabama (AL)",
            "Alaska (AK)",
            "Arizona (AZ)",
            "Arkansas (AR)",
            "California (CA)",
            "Colorado (CO)",
            "Connecticut (CT)",
            "Delaware (DE)",
            "District of Columbia (DC)",
            "Florida (FL)",
            "Georgia (GA)",
            "Hawaii (HI)",
            "Idaho (ID)",
            "Illinois (IL)",
            "Indiana (IN)",
            "Iowa (IA)",
            "Kansas (KS)",
            "Kentucky (KY)",
            "Louisiana (LA)",
            "Maine (ME)",
            "Maryland (MD)",
            "Massachusetts (MA)",
            "Michigan (MI)",
            "Minnesota (MN)",
            "Mississippi (MS)",
            "Missouri (MO)",
            "Montana (MT)",
            "Nebraska (NE)",
            "Nevada (NV)",
            "New Hampshire (NH)",
            "New Jersey (NJ)",
            "New Mexico (NM)",
            "New York (NY)",
            "North Carolina (NC)",
            "North Dakota (ND)",
            "Ohio (OH)",
            "Oklahoma (OK)",
            "Oregon (OR)",
            "Pennsylvania (PA)",
            "Rhode Island (RI)",
            "South Carolina (SC)",
            "South Dakota (SD)",
            "Tennessee (TN)",
            "Texas (TX)",
            "Utah (UT)",
            "Vermont (VT)",
            "Virginia (VA)",
            "Washington (WA)",
            "West Virginia (WV)",
            "Wisconsin (WI)",
            "Wyoming (WY)"
        };
        return stateStrings;
    }
	

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getActionCommand().equals("add") || e.getActionCommand().equals("delete") || e.getActionCommand().equals("update") || e.getActionCommand().equals("search"))
		{
			if("add".equals(e.getActionCommand()))
			{
				if(maxRow == -1)
				{
					fileContents[0][0] = "Name";
					fileContents[0][1] = "Street";
					fileContents[0][2] = "City";
					fileContents[0][3] = "State";
					fileContents[0][4] = "Zip";
					fileContents[0][5] = "Phone Number";
					maxRow++;
				}
				maxRow++;
				fileContents[maxRow][0] = nameField.getText();
				fileContents[maxRow][1] = streetField.getText();
				fileContents[maxRow][2] = cityField.getText();
				fileContents[maxRow][3] = (String)stateBox.getSelectedItem();
				fileContents[maxRow][4] = zipField.getText();
				fileContents[maxRow][5] = phoneField.getText();
				writeFile();
				nameField.setText("");
				streetField.setText("");
				cityField.setText("");
				zipField.setText("");
				phoneField.setText("");
				stateBox.setSelectedIndex(0);
			}
			else if("search".equals(e.getActionCommand()))
			{
				listModel.clear();
				findResults();		
			}
			else if("update".equals(e.getActionCommand()))
			{
				fileContents[selectedRow][0] = nameField.getText();
				fileContents[selectedRow][1] = streetField.getText();
				fileContents[selectedRow][2] = cityField.getText();
				fileContents[selectedRow][3] = (String)stateBox.getSelectedItem();
				fileContents[selectedRow][4] = zipField.getText();
				fileContents[selectedRow][5] = phoneField.getText();
				writeFile();
				nameField.setText("");
				streetField.setText("");
				cityField.setText("");
				zipField.setText("");
				phoneField.setText("");
				listModel.clear();
				stateBox.setSelectedIndex(0);
			}
			else if ("delete".equals(e.getActionCommand()))
			{
				fileContents[selectedRow][0] = "*DELETED*";
				nameField.setText("");
				streetField.setText("");
				cityField.setText("");
				zipField.setText("");
				phoneField.setText("");
				writeFile();
				listModel.clear();
			}
			update.setVisible(false);
			delete.setVisible(false);
			add.setVisible(true);
			searchField.setText("");
		}
	}
	

	protected void selectItLater(Component c) {
        if (c instanceof JFormattedTextField) {
            final JFormattedTextField ftf = (JFormattedTextField)c;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ftf.selectAll();
                }
            });
        }
    }
	
	public void findResults()
	{
		int resultCount = 0;
		for(int r = 1; r <=maxRow;r++)
		{
			for(int c = 0;c<fileContents[0].length;c++)
			{
				if(fileContents[r][c].toLowerCase().contains(searchField.getText().toLowerCase()) && fileContents[r][0] != "*DELETED*")
				{
					
					 listModel.insertElementAt(fileContents[r][0],resultCount);
					 resultRow[resultCount] = r;
					 resultCount++;				 
					 break;
				}
			}
		}
	}

/* 
                     Read in CSV to fileContent Array    (note limited to 100 rows)  	
 */
	
  	public void readFile()
	{
  		 BufferedReader br = null;
  	     String line = "";
         String cvsSplitBy = ",";
         int r = -1;
    try {
        br = new BufferedReader(new FileReader("AddressBook.csv"));
        while ((line = br.readLine()) != null) 
        {
            r++;
            // use comma as separator
            String[] fields = line.split(cvsSplitBy);     
         	fileContents[r][0] = fields[0];
         	fileContents[r][1] = fields[1];
         	fileContents[r][2] = fields[2];
         	fileContents[r][3] = fields[3];
         	fileContents[r][4] = fields[4];
         	fileContents[r][5] = fields[5];
         }
            maxRow = r;

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }	
}


	//Writes stuff to File
	public void writeFile()
	{
		PrintWriter writer = null;
		try {
		    writer = new PrintWriter("AddressBook.csv");
		} catch (FileNotFoundException ex) {
		    ex.printStackTrace();
		    
		}
		StringBuilder builder = new StringBuilder();
		for(int r = 0; r <=maxRow;r++)
		{
			if(!fileContents[r][0].equals("*DELETED*"))
			{
				for(int c = 0;c<fileContents[0].length;c++)
				{
					
					if(c==fileContents[0].length-1)
						builder.append(fileContents[r][c]);
					else
						builder.append(fileContents[r][c] + ",");
				}
				builder.append("\n");
			}
		}
		writer.append(builder.toString());
		writer.close();
	}
	
	private static void createAndShowGUI() 
	{
        //Create and set up the window.
        JFrame frame = new JFrame("Your Address Book");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        frame.add(new AddressBook());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

	@Override
	public void valueChanged(ListSelectionEvent e) 
	{       if(!results.isSelectionEmpty())
			{
				selectedRow = resultRow[results.getSelectedIndex()];
				nameField.setText(fileContents[selectedRow][0]);
				streetField.setText(fileContents[selectedRow][1]);
				cityField.setText(fileContents[selectedRow][2]);
				stateBox.setSelectedItem(fileContents[selectedRow][3]);
				zipField.setText(fileContents[selectedRow][4]);
				phoneField.setText(fileContents[selectedRow][5]);
				update.setVisible(true);
				delete.setVisible(true);
				add.setVisible(false);
			}
	}
	
}
